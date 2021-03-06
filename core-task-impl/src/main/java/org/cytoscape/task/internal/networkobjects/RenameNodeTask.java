package org.cytoscape.task.internal.networkobjects;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class RenameNodeTask extends AbstractGetTask {
	@Tunable(description="Network node is in", context="nogui")
	public CyNetwork network = null;

	@Tunable(description="Node to be renamed", context="nogui")
	public String node = null;

	@Tunable(description="New node name", context="nogui")
	public String newName = null;

	public RenameNodeTask() {
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
			return;
		}

		if (node == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Node name or suid must be specified");
			return;
		}

		if (newName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "New name must be specified");
			return;
		}

		CyNode renamedNode = getNode(network, node);
		network.getRow(renamedNode).set(CyNetwork.NAME, newName);
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Node "+renamedNode+" renamed to "+newName);
	}
}
