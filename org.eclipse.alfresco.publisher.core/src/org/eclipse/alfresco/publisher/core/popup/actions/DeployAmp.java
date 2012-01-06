package org.eclipse.alfresco.publisher.core.popup.actions;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoDeployementException;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployAmp extends AlfrescoDeploy {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DeployAmp.class);

	@Override
	protected void deploy(IProject project, AlfrescoPreferenceHelper preferences, IProgressMonitor monitor)  {
		
		String alfrescoHome = preferences.getAlfrescoHome();
		String webappName = preferences.getWebappName();
		String ampRelativePath = preferences.getTargetAmpLocation() + ".amp";
		final String webappAbsolutePath = preferences.getWebappAbsolutePath();
		
		insurePathOK (webappName, webappAbsolutePath, preferences.getServerPath());

        
		String mmtAbsolutePath = alfrescoHome + File.separator + "bin" + File.separator + "alfresco-mmt.jar";
		
		
		try {
			monitor.setTaskName("Deleting old webapp");
			FileUtils.deleteDirectory(webappAbsolutePath);
		} catch (IOException e1) {
			throw new AlfrescoDeployementException(e1);
		}
		
		File ampAbsolutePath = project.getFile(ampRelativePath).getLocation().toFile();

		ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar",
				mmtAbsolutePath, "install", ampAbsolutePath.getAbsolutePath(), webappAbsolutePath+".war",
				"-force");
		
		try {
			monitor.setTaskName("Applying amp.");
			Process process = processBuilder.start();
			process.waitFor();
		} catch (IOException e) {
			throw new AlfrescoDeployementException(e);
		} catch (InterruptedException e) {
			throw new AlfrescoDeployementException(e);
		}

	}

	private void insurePathOK (String webappName, String webappAbsolutePath, String serverPath) {
		if(StringUtils.isEmpty(webappName)) {
			throw new AlfrescoDeployementException("Webapp name cannot be null");
		}
		
		if(StringUtils.isEmpty(serverPath)) {
			throw new AlfrescoDeployementException("Server path cannot be null");
		}
		
		if(!webappAbsolutePath.endsWith("/webapps/" + webappName)) {
			throw new AlfrescoDeployementException("Path to webapp suspisious: " + webappAbsolutePath);
		}
		if(!webappAbsolutePath.startsWith(serverPath))
			throw new AlfrescoDeployementException("Webapp path not under serverPath: " + webappAbsolutePath + " : " + serverPath);
		
	}

	@Override
	protected String getGoals() {
		return "package -Pamp";
	}

}
