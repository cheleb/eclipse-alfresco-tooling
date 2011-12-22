package org.eclipse.alfresco.publisher.core;

import java.io.File;

import org.eclipse.core.resources.IResource;

public interface Deployer {
	
	public ResourceCommand getFile(IResource resource);
	
	public File getClasses();
	
	public String getPathRelativeToClasses(String projectRelativePath);

	public void addResource(IResource resource);

	public void removeResource(IResource resource);

	public void updateResource(IResource resource);
}
