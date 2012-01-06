package org.eclipse.alfresco.publisher.core.popup.actions;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployJar extends AlfrescoDeploy {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeployJar.class);

	@Override
	protected void deploy(IProject project, AlfrescoPreferenceHelper preferences, IProgressMonitor monitor) throws IOException {
		String jarName = preferences.getAmpJarName();
		String jarLocation = preferences.getAmpJarLocation();
		
		String libFolder = preferences.getAmpLib();
		
		IFile src = project.getFile(jarLocation);
		File dst = new File(libFolder, jarName);
		
		FileUtils.copyFile(src.getLocation().toFile(), dst);
		
		
	}

	@Override
	protected String getGoals() {
		return "package";
	}
	
}
