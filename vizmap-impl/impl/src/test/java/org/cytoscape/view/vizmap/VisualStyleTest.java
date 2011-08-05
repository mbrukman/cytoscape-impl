package org.cytoscape.view.vizmap;

import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Properties;

import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.vizmap.internal.VisualLexiconManager;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.cytoscape.test.support.NetworkViewTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;

import org.junit.After;
import org.junit.Before;

public class VisualStyleTest extends AbstractVisualStyleTest {

	@Before
	public void setUp() throws Exception {
		final Class<String> type = String.class;
        CyProperty<Properties> cyProperties = mock(CyProperty.class);
        NetworkViewTestSupport nvts = new NetworkViewTestSupport(cyProperties);
        network = nvts.getNetworkFactory().getInstance();

        node1 = network.addNode();
        node2 = network.addNode();
        node3 = network.addNode();

        edge = network.addEdge(node1, node2, true);
        CyTable nodeTable = network.getDefaultNodeTable();
        nodeTable.createColumn(attrName, String.class, true);
        nodeTable.getRow(node1.getSUID()).set(attrName, "red");
        nodeTable.getRow(node2.getSUID()).set(attrName, "green");
        nodeTable.getRow(node3.getSUID()).set(attrName, "foo");

        networkView = nvts.getNetworkViewFactory().getNetworkView(network);
		
		// Create root node.
		final VisualLexiconManager lexManager = mock(VisualLexiconManager.class);
		
		
		// Create root node.
		final NullVisualProperty minimalRoot = new NullVisualProperty("MINIMAL_ROOT", "Minimal Root Visual Property");
		final MinimalVisualLexicon minimalLex = new MinimalVisualLexicon(minimalRoot);
		final Set<VisualLexicon> lexSet = new HashSet<VisualLexicon>();
		lexSet.add(minimalLex);
		final Collection<VisualProperty<?>> nodeVP = minimalLex.getAllDescendants(MinimalVisualLexicon.NODE);
		final Collection<VisualProperty<?>> edgeVP = minimalLex.getAllDescendants(MinimalVisualLexicon.EDGE);
		when(lexManager.getNodeVisualProperties()).thenReturn(nodeVP);
		when(lexManager.getEdgeVisualProperties()).thenReturn(edgeVP);
		
		when(lexManager.getAllVisualLexicon()).thenReturn(lexSet);
		
		final VisualStyleFactoryImpl visualStyleFactory = new VisualStyleFactoryImpl(lexManager);
		originalTitle = "Style 1";
		newTitle = "Style 2";
		style = visualStyleFactory.getInstance(originalTitle);
	}
}
