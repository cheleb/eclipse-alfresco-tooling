package org.eclipse.alfresco.publisher.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class AlfrescoPublisherUIActivator extends AbstractUIPlugin {

	private static AlfrescoPublisherUIActivator INSTANCE;
	
	public AlfrescoPublisherUIActivator() {
		INSTANCE = this;
	}

	public static AlfrescoPublisherUIActivator getDefault() {
		return INSTANCE;
	}

	
	
}
