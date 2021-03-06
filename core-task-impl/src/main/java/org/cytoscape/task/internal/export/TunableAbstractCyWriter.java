package org.cytoscape.task.internal.export;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.CyWriterManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;
import org.cytoscape.work.util.ListChangeListener;
import org.cytoscape.work.util.ListSelection;
import org.cytoscape.work.util.ListSingleSelection;

/**
 * An abstract utility implementation of a Task that writes a user defined file
 * to a file type determined by a provided writer manager.
 * This class is meant to be extended for specific file types such that the appropriate
 * {@link org.cytoscape.io.write.CyWriter} can be identified.
 */
public abstract class TunableAbstractCyWriter<S extends CyWriterFactory, T extends CyWriterManager<S>> extends
		AbstractCyWriter<S, T> implements TunableValidator, RequestsUIHelper {

	/**
	 * The list of file type options generated by the file types available from
	 * the CyWriterManager. This field should not be set directly, but rather
	 * handled by the {@link org.cytoscape.work.Tunable} processing.
	 */
	@Tunable(description = "Select the export file format:", gravity = 1.0)
	public ListSingleSelection<String> options;

	@ContainsTunables(offset = 1.0)
	public CyWriter writer = null;
	
	@Override
	public CyWriter getWriter() {
		if(writer == null) {
			try {
				writer = getWriter(getFileFilter(getExportFileFormat()));
			}
			catch(Exception e) {}
		}
		return writer;
	}
	
	/**
	 * The method sets the file to be written.  This field should not
	 * be called directly, but rather handled by the {@link org.cytoscape.work.Tunable}
	 * processing. This method is the "setter" portion of a
	 * getter/setter tunable method pair.
	 * @param f The file to be written.
	 */
	public final void setOutputFile(File f) {
		if(f == null || fileExtensionIsOk(f))
			outputFile = f;
		else {
			outputFile = addOrReplaceExtension(f);
			if(helper != null)
				helper.update(this);
		}
	}

	@Override
	protected final String getExportFileFormat() {
		return options.getSelectedValue();
	}
	
	protected TunableUIHelper helper = null;

	/**
	 * @param writerManager
	 *            The CyWriterManager to be used to determine which
	 *            {@link org.cytoscape.io.write.CyWriter} to be used to write
	 *            the file chosen by the user.
	 */
	public TunableAbstractCyWriter(T writerManager, CyApplicationManager cyApplicationManager) {
		super(writerManager, cyApplicationManager);
		final List<String> availableFormats = new ArrayList<>(getFileFilterDescriptions());
		options = new ListSingleSelection<String>(availableFormats);
		options.addListener(new ListChangeListener<String>() {
			@Override
			public void selectionChanged(ListSelection<String> source) {
				try {
					writer = getWriter(getFileFilter(getExportFileFormat()));
					if(outputFile != null)
						outputFile = addOrReplaceExtension(outputFile);
					if(helper != null)
						helper.refresh(TunableAbstractCyWriter.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void listChanged(ListSelection<String> source) {
			}
		});
	}

	@Override
	public ValidationState getValidationState(final Appendable msg) {
		if (getExportFileFormat() == null) {
			try {
				msg.append("Select a file type.");
			} catch (final Exception e) {
				/* Intentionally empty. */
			}

			return ValidationState.INVALID;
		}

		if (outputFile == null) {
			try {
				msg.append("Enter a file name.");
			} catch (final Exception e) {
				/* Intentionally empty. */
			}

			return ValidationState.INVALID;
		}
		
		if (outputFile.exists()) {
			try {
				msg.append("File already exists, are you sure you want to overwrite it?");
			} catch (final Exception e) {
				/* Intentionally empty. */
			}

			return ValidationState.REQUEST_CONFIRMATION;
		} else {
			return ValidationState.OK;
		}
	}
	
	@Override
	public void setUIHelper(TunableUIHelper helper) {
		this.helper = helper;
	}
}
