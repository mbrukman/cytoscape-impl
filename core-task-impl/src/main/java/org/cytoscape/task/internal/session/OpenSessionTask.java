package org.cytoscape.task.internal.session;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionLoadCancelledEvent;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 * Call the session reader and read everything in the zip archive.<br>
 * setAcceleratorCombo(java.awt.event.KeyEvent.VK_O, ActionEvent.CTRL_MASK);
 */
public class OpenSessionTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Open Session";
	}
	
	private CySessionReader reader;
	
	private final File sessionFile;
	private final CyServiceRegistrar serviceRegistrar;

	public OpenSessionTask(CyServiceRegistrar serviceRegistrar) {
		this(null, serviceRegistrar);
	}
	
	public OpenSessionTask(File sessionFile, CyServiceRegistrar serviceRegistrar) {
		this.sessionFile = sessionFile;
		this.serviceRegistrar = serviceRegistrar;
	}

	/**
	 * Clear current session and open the cys file.
	 */
	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
		final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
		
		if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
			loadSession(this);
		else
			insertTasksAfterCurrentTask(new OpenSessionWithWarningTask());
	}
	
	private void loadSession(AbstractTask currentTask) {
		if (sessionFile == null) {
			// Should use Tunables to show a file chooser and let the user select the file
			getTaskIterator().insertTasksAfter(currentTask, new OpenSessionWithoutWarningTask());
		} else {
			// Should not show the Tunables dialog
			final Map<String, Object> m = new HashMap<>();
			m.put("file", sessionFile);
	
			final TunableSetter tunableSetter = serviceRegistrar.getService(TunableSetter.class);
			getTaskIterator().insertTasksAfter(currentTask,
					tunableSetter.createTaskIterator(new TaskIterator(new OpenSessionWithoutWarningTask()), m));
		}
	}
	
	public final class OpenSessionWithoutWarningTask extends AbstractTask {
		
		@Tunable(description="Session file to load:", params="fileCategory=session;input=true")
		public File file;
		
		private Set<CyNetwork> currentNetworkSet = new HashSet<>();
		private Set<CyGroup> currentGroupSet = new HashSet<>();

		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			eventHelper.fireEvent(new SessionAboutToBeLoadedEvent(this));
			
			try {
				try {
					taskMonitor.setStatusMessage("Opening Session File.\n\nIt may take a while.\nPlease wait...");
					taskMonitor.setProgress(0.0);
		
					if (file == null)
						throw new NullPointerException("No file specified.");
					
					reader = serviceRegistrar.getService(CySessionReaderManager.class)
							.getReader(file.toURI(), file.getName());
					
					if (reader == null)
						throw new NullPointerException("Failed to find appropriate reader for file: " + file);
					
					// Save the current network and group set, in case loading the new session is cancelled later
					final CyNetworkTableManager netTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
					currentNetworkSet.addAll(netTableManager.getNetworkSet());
					
					final CyGroupManager grManager = serviceRegistrar.getService(CyGroupManager.class);
					
					for (final CyNetwork n : currentNetworkSet)
						currentGroupSet.addAll(grManager.getGroupSet(n));
					
					taskMonitor.setProgress(0.2);
					reader.run(taskMonitor);
					taskMonitor.setProgress(0.8);
				} catch (Exception e) {
					reader = null;
					eventHelper.fireEvent(new SessionLoadCancelledEvent(this, e));
					throw e;
				}
				
				if (cancelled) {
					disposeCancelledSession();
				} else {
					try {
						changeCurrentSession(taskMonitor);
					} catch (Exception e) {
						eventHelper.fireEvent(new SessionLoadCancelledEvent(this, e));
						throw e;
					}
				}
			} finally {
				// plug big memory leak
				reader = null;
			}
		}
		
		@Override
		public void cancel() {
			super.cancel();
			
			if (reader != null)
				reader.cancel(); // Remember to cancel the Session Reader!
			
			serviceRegistrar.getService(CyEventHelper.class).fireEvent(new SessionLoadCancelledEvent(this));
		}
		
		private void changeCurrentSession(final TaskMonitor taskMonitor) throws Exception {
			final CySession newSession = reader.getSession();
			
			if (newSession == null)
				throw new NullPointerException("Session could not be read for file: " + file);

			serviceRegistrar.getService(CySessionManager.class).setCurrentSession(newSession, file.getAbsolutePath());
			
			taskMonitor.setProgress(1.0);
			taskMonitor.setStatusMessage("Session file " + file + " successfully loaded.");
			
			// Add this session file URL as the most recent file.
			serviceRegistrar.getService(RecentlyOpenedTracker.class).add(file.toURI().toURL());
		}
		
		private void disposeCancelledSession() {
			final CySession newSession = reader.getSession();
			
			if (newSession != null) {
				for (final CyNetworkView view : newSession.getNetworkViews())
					view.dispose();
			}
			
			if (currentNetworkSet != null) {
				// Dispose cancelled networks and groups:
				// This is necessary because the new CySession contains only registered networks;
				// unregistered networks (e.g. CyGroup networks) may have been loaded and need to be disposed as well.
				// The Network Table Manager should contain all networks, including the unregistered ones.
				final CyNetworkTableManager netTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
				final Set<CyNetwork> newNetworkSet = new HashSet<>(netTableManager.getNetworkSet());
				
				final CyGroupManager grManager = serviceRegistrar.getService(CyGroupManager.class);
				
				for (final CyNetwork net : newNetworkSet) {
					if (!currentNetworkSet.contains(net)) {
						for (final CyGroup gr : grManager.getGroupSet(net)) {
							if (currentGroupSet != null && !currentGroupSet.contains(gr))
								grManager.destroyGroup(gr);
						}
						
						net.dispose();
					}
				}
				
				currentGroupSet = null;
				currentNetworkSet = null;
			}
		}
	}
	
	public final class OpenSessionWithWarningTask extends AbstractTask {
		
		@Tunable(description="<html>Current session (all networks and tables) will be lost.<br />Do you want to continue?</html>",
				 params="ForceSetDirectly=true;ForceSetTitle=Open Session")
		public boolean loadSession;
		
		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			if (loadSession)
				loadSession(this);
		}
	}
}