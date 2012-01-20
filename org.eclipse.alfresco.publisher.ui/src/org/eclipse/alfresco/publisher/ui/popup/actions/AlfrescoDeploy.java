package org.eclipse.alfresco.publisher.ui.popup.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.alfresco.publisher.core.AlfrescoFileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.MavenLaunchHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AlfrescoDeploy implements IObjectActionDelegate {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AlfrescoDeploy.class);

	private Shell shell;
	private ISelection selection;

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		// MessageDialog.openInformation(
		// shell,
		// "Maven Integration for Eclipse Settings",
		// "Deploy was executed.");

		final IProject project = getProject();

		if (project == null) {
			return;
		}

		final AlfrescoPreferenceHelper preferences = new AlfrescoPreferenceHelper(
				project);

		final AlfrescoFileUtils alfrescoFileUtils = new AlfrescoFileUtils(
				preferences.getServerPath(), preferences.getWebappName());

		IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Reloading", 4);

				boolean deploymentIncrematalCanceled = shoulDeactivateIncrementalDeployement()
						&& preferences.isIncrementalDeploy();

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
					if (deploymentIncrematalCanceled) {
						preferences.setIncrementalDeploy(false);
						preferences.flush();
					}

					monitor.subTask("Stopping server");
					stopServer(preferences);
					if (monitor.isCanceled()) {
						LOGGER.info("Canceled");
						return;
					}
					monitor.worked(1);
					monitor.subTask("Invoking build");
					build(project, monitor);
					monitor.worked(1);
					if (monitor.isCanceled()) {
						LOGGER.info("Canceled");
						return;
					}
					monitor.subTask("Deploy AMP");
					deploy(project, alfrescoFileUtils, preferences, monitor);
					monitor.worked(1);
					monitor.subTask("Starting server");
					if (monitor.isCanceled()) {
						LOGGER.info("Canceled");
						return;
					}
					startServer(preferences);
					monitor.worked(1);
				} catch (IOException e) {
					throw new OperationCanceledException(
							e.getLocalizedMessage());
				} catch (BackingStoreException e) {
					throw new OperationCanceledException(
							e.getLocalizedMessage());
				} finally {
					if (deploymentIncrematalCanceled) {
						preferences.setIncrementalDeploy(true);
						try {
							preferences.flush();
						} catch (BackingStoreException e) {
							throw new OperationCanceledException(
									e.getLocalizedMessage());
						}
					}
				}

			}
		};

		try {
			new ProgressMonitorDialog(shell).run(true, true,
					iRunnableWithProgress);
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getLocalizedMessage(), e.getCause());
		} catch (InterruptedException e) {
			LOGGER.error(e.getLocalizedMessage(), e.getCause());
		}

	}

	private void startServer(AlfrescoPreferenceHelper preferences)
			throws IOException {
		ProcessBuilder processBuilder = null;
		Map<String, String> environment = null;
		if (preferences.isAlfresco()) {
			processBuilder = new ProcessBuilder("scripts/ctl.sh", "start");
			environment = processBuilder.environment();
			environment.put("CATALINA_PID", preferences.getServerPath()
					+ "/temp/catalina.pid");
			// processBuilder = new ProcessBuilder("bin/startup.sh");
			// processBuilder.directory(new File(serverPath).getParentFile());

		} else {
			processBuilder = new ProcessBuilder("bin/startup.sh");
			environment = processBuilder.environment();
			environment
					.put("JAVA_OPTS",
							"-XX:MaxPermSize=512m -Xms128m -Xmx768m -Dalfresco.home=/Applications/alfresco-4.0.b -Dcom.sun.management.jmxremote -Dsun.security.ssl.allowUnsafeRenegotiation=true");
		}
		processBuilder.directory(new File(preferences.getServerPath()));

		Process start = processBuilder.start();
		try {
			start.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected IProject getProject() {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object object = structuredSelection.getFirstElement();

			if (object instanceof IProject) {
				return (IProject) object;
			}
		}
		return null;

	}

	protected abstract void deploy(IProject project,
			AlfrescoFileUtils alfrescoFileUtils,
			AlfrescoPreferenceHelper preferences, IProgressMonitor monitor)
			throws IOException;

	private void build(IProject project, final IProgressMonitor monitor) {

		String goals = getGoals();

		final ILaunchConfiguration launchConf = MavenLaunchHelper
				.createLaunchConfiguration(project, goals);

		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				DebugUITools.launch(launchConf, ILaunchManager.RUN_MODE);
				IProcess currentProcess = DebugUITools.getCurrentProcess();

				while (!currentProcess.isTerminated()) {
					try {
						Thread.sleep(1000);
						LOGGER.debug(currentProcess.getLabel() + " " + currentProcess.canTerminate());
						if(true && currentProcess.canTerminate())
							currentProcess.terminate();
					} catch (InterruptedException e) {
						LOGGER.error(e.getLocalizedMessage(), e);
						throw new RuntimeException(e);
					} catch (DebugException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (monitor.isCanceled()) {

						try {
							currentProcess.terminate();
						} catch (DebugException e) {
							LOGGER.error(e.getLocalizedMessage(), e);
							throw new RuntimeException(e);
						}

					}
				}

			}
		});

	}

	protected abstract boolean shoulDeactivateIncrementalDeployement();

	protected abstract String getGoals();

	private void stopServer(AlfrescoPreferenceHelper preferences)
			throws IOException {

		ProcessBuilder processBuilder;
		if (preferences.isAlfresco()) {
			processBuilder = new ProcessBuilder("scripts/ctl.sh", "stop");
		} else {

			processBuilder = new ProcessBuilder(
					"java",
					"-cp",
					"bin/bootstrap.jar:bin/commons-daemon.jar:bin/tomcat-juli.jar",
					"org.apache.catalina.startup.Bootstrap", "stop");
		}
		processBuilder.directory(new File(preferences.getServerPath()));

		Process start = processBuilder.start();
		try {
			int r = start.waitFor();
			LOGGER.info("Stopping "
					+ (preferences.isAlfresco() ? "alfresco" : "share") + ": "
					+ (r == 0 ? "OK" : "ERROR"));
		} catch (InterruptedException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
		}

	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}

}
