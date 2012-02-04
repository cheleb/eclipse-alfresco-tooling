package org.eclipse.alfresco.publisher.ui.popup.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.alfresco.publisher.core.AlfrescoFileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.OperationCanceledException;
import org.eclipse.alfresco.publisher.core.helper.ServerHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMPDeployRunnable implements IRunnableWithProgress {
	
	
	private static final int DEPLOY_N_TASK = 4;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AMPDeployRunnable.class);
	
	private IProject project;
	
	private boolean deploymentIncrementalCanceled;

	private AlfrescoPreferenceHelper preferences;

	private AlfrescoDeploy alfrescoDeploy;

	private AlfrescoFileUtils alfrescoFileUtils;

	public AMPDeployRunnable(AlfrescoDeploy alfrescoDeploy, AlfrescoFileUtils alfrescoFileUtils, IProject project, AlfrescoPreferenceHelper preferences, boolean deploymentIncrementalCanceled) {
		this.alfrescoDeploy = alfrescoDeploy;
		this.alfrescoFileUtils = alfrescoFileUtils;
		this.project = project;
		this.preferences = preferences;
		this.deploymentIncrementalCanceled = deploymentIncrementalCanceled;
	}

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Reloading", DEPLOY_N_TASK);

		
		IFile logFile = project.getFile("target/deployed.log");
		if (logFile.exists()) {
			try {
				logFile.delete(true, monitor);
			} catch (CoreException e) {
				LOGGER.error(logFile.getProjectRelativePath()
						.toOSString() + " could not be reset.", e);
			}
		}

		try {
			doDeploy(monitor);
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new OperationCanceledException(
					e.getLocalizedMessage(), e);
		} catch (BackingStoreException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new OperationCanceledException(
					e.getLocalizedMessage(), e);
		} finally {
			if (deploymentIncrementalCanceled) {
				preferences.setIncrementalDeploy(true);
				try {
					preferences.flush();
				} catch (BackingStoreException e) {
					throw new OperationCanceledException(
							e.getLocalizedMessage(), e);
				}
			}
		}

	}

	private void doDeploy(IProgressMonitor monitor)
			throws BackingStoreException, IOException {
		if (deploymentIncrementalCanceled) {
			preferences.setIncrementalDeploy(false);
			preferences.flush();
		}

		monitor.subTask("Stopping server");
		ServerHelper.stopServer(preferences);
		if (monitor.isCanceled()) {
			LOGGER.info("Canceled");
			return;
		}
		monitor.worked(1);
		monitor.subTask("Invoking build");
		alfrescoDeploy.build(project, monitor);
		monitor.worked(1);
		if (monitor.isCanceled()) {
			LOGGER.info("Canceled");
			return;
		}
		monitor.subTask("Deploy AMP");
		alfrescoDeploy.deploy(project, alfrescoFileUtils, preferences, monitor);
		monitor.worked(1);
		monitor.subTask("Starting server");
		if (monitor.isCanceled()) {
			LOGGER.info("Canceled");
			return;
		}

		alfrescoDeploy.cleanBackupFile(preferences, monitor);

		ServerHelper.startServer(preferences);
		monitor.worked(1);
	}
}
