package org.eclipse.alfresco.publisher.ui.popup.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.alfresco.publisher.core.AlfrescoDeployementException;
import org.eclipse.alfresco.publisher.core.AlfrescoFileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployAmp extends AlfrescoDeploy {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DeployAmp.class);

	@Override
	protected void deploy(IProject project,
			AlfrescoFileUtils alfrescoFileUtils,
			AlfrescoPreferenceHelper preferences, IProgressMonitor monitor) {

		
		String alfrescoHome = preferences.getAlfrescoHome();

		String ampRelativePath = preferences.getTargetAmpLocation() + ".amp";
		LOGGER.info("Deploying AMP " + ampRelativePath);
		final String webappAbsolutePath = preferences.getWebappAbsolutePath();

		String mmtAbsolutePath = alfrescoHome + File.separator + "bin"
				+ File.separator + "alfresco-mmt.jar";

		try {
			monitor.setTaskName("Deleting old webapp");

			alfrescoFileUtils.rm(webappAbsolutePath);
		} catch (IOException e1) {
			throw new AlfrescoDeployementException(e1);
		}

		File ampAbsolutePath = project.getFile(ampRelativePath).getLocation()
				.toFile();

		ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar",
				mmtAbsolutePath, "install", ampAbsolutePath.getAbsolutePath(),
				webappAbsolutePath + ".war", "-force");

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

	@Override
	protected String getGoals() {
		return "package -Pamp";
	}

	@Override
	protected boolean shoulDeactivateIncrementalDeployement() {
		return true;
	}

}
