package org.eclipse.alfresco.publisher.core;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IResource;

public interface Deployer {
	
	ResourceCommand getResourceCommand(IResource resource, int kind);
	
	File getClasses();
	
	String getPathRelativeToClasses(String projectRelativePath);

	void addResource(ResourceCommand resourceCommand);

	void removeResource(ResourceCommand resourceCommand);

	void updateResource(ResourceCommand resource);
	
	String getRoot();

	Map<String, ResourceCommand> getDelayedJarResourceCommands();

	
}
