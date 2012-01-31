package org.eclipse.alfresco.publisher.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class AlfrescoPublisherUIActivator extends AbstractUIPlugin {

	private static AlfrescoPublisherUIActivator instance;
	
	public AlfrescoPublisherUIActivator() {
		instance = this;
	}

	public static AlfrescoPublisherUIActivator getDefault() {
		return instance;
	}

	
	
}
