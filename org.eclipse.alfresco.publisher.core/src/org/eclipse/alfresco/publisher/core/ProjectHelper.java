package org.eclipse.alfresco.publisher.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;

public class ProjectHelper {

	public static IProject getProject(IAdaptable element) {
		IAdaptable adaptable = (IAdaptable) element;

		return (IProject) adaptable.getAdapter(IProject.class);
		
	}

}
