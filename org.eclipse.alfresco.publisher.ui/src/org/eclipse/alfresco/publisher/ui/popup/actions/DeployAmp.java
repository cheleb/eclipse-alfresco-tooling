package org.eclipse.alfresco.publisher.ui.popup.actions;

import java.io.IOException;

import org.eclipse.alfresco.publisher.core.AlfrescoDeployementException;
import org.eclipse.alfresco.publisher.core.AlfrescoFileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.helper.AlfrescoMMTHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class DeployAmp extends AlfrescoDeploy {



	@Override
	protected void deploy(IProject project,
			AlfrescoFileUtils alfrescoFileUtils,
			AlfrescoPreferenceHelper preferences, IProgressMonitor monitor) {

		AlfrescoMMTHelper alfrescoMMTHelper = new AlfrescoMMTHelper(project,
				alfrescoFileUtils);

		monitor.setTaskName("Deleting old webapp");
		alfrescoMMTHelper.deleteExplodedWar();

		ProcessBuilder processBuilder = alfrescoMMTHelper
				.getApplyAMPProcessBuilder();

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
