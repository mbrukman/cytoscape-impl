
package org.cytoscape.work.internal.submenu;

import org.cytoscape.work.TunableMutator;
import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.SubmenuTunableHandler;

import java.util.List;
import java.util.Map;
import javax.swing.JMenuItem;

public class SubmenuTunableMutator extends AbstractTunableInterceptor<SubmenuTunableHandler> 
	implements TunableMutator<SubmenuTunableHandler,JMenuItem> {
	
	private final DialogTaskManager dtm;

	public SubmenuTunableMutator(DialogTaskManager dtm) {
		this.dtm = dtm;
	}

	public void setConfigurationContext(Object o) {
		// no-op 
	}

	public JMenuItem buildConfiguration(Object objectWithTunables) {
		TaskFactory tf;
		if ( objectWithTunables instanceof TaskFactory )
			tf = (TaskFactory)objectWithTunables;
		else
			return null;

		for ( SubmenuTunableHandler handler : getHandlers(tf) ) {
			handler.setExecutionParams(dtm,tf);
			handler.handle();
			return handler.getSubmenuItem();
		}
		return null;
	}

	public boolean validateAndWriteBack(Object objs) {
	 	return true;	
	}

}

