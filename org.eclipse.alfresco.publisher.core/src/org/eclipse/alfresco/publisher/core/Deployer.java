package org.eclipse.alfresco.publisher.core;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;

public interface Deployer {
	
	public ResourceCommand getResourceCommand(IResource resource, int kind);
	
	public File getClasses();
	
	public String getPathRelativeToClasses(String projectRelativePath);

	public void addResource(ResourceCommand resourceCommand);

	public void removeResource(ResourceCommand resourceCommand);

	public void updateResource(ResourceCommand resource);
	
	public String getRoot();

	public Map<String, ResourceCommand> getDelayedJarResourceCommands();

	
}
