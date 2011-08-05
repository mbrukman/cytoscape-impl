package org.cytoscape.biopax.internal;

import java.io.InputStream;

import org.cytoscape.biopax.NetworkListener;
import org.cytoscape.biopax.internal.view.BioPaxContainerImpl;
import org.cytoscape.biopax.util.BioPaxVisualStyleUtil;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class BioPaxNetworkViewTaskFactory implements InputStreamTaskFactory {

	private final CyFileFilter filter;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkNaming naming;
	private final BioPaxContainerImpl bpContainer;
	private final NetworkListener networkListener;

	private InputStream inputStream;
	private String inputName;
	private VisualMappingManager mappingManager;
	private BioPaxVisualStyleUtil bioPaxVisualStyleUtil;

	public BioPaxNetworkViewTaskFactory(CyFileFilter filter, CyNetworkFactory networkFactory, CyNetworkViewFactory viewFactory, CyNetworkNaming naming, BioPaxContainerImpl bpContainer, NetworkListener networkListener, VisualMappingManager mappingManager, BioPaxVisualStyleUtil bioPaxVisualStyleUtil) {
		this.filter = filter;
		this.networkFactory = networkFactory;
		this.viewFactory = viewFactory;
		this.naming = naming;
		this.bpContainer = bpContainer;
		this.networkListener = networkListener;
		this.mappingManager = mappingManager;
		this.bioPaxVisualStyleUtil = bioPaxVisualStyleUtil;
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		BioPaxNetworkViewReaderTask task = new BioPaxNetworkViewReaderTask(inputStream, inputName, networkFactory, viewFactory, naming, bpContainer, networkListener, mappingManager, bioPaxVisualStyleUtil);
		return new TaskIterator(task);
	}

	@Override
	public CyFileFilter getCyFileFilter() {
		return filter;
	}

	@Override
	public void setInputStream(InputStream inputStream, String inputName) {
		this.inputStream = inputStream;
		this.inputName = inputName;
	}

}
