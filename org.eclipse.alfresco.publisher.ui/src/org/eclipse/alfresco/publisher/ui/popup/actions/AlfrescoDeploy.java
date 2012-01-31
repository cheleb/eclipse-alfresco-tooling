package org.eclipse.alfresco.publisher.ui.popup.actions;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.alfresco.publisher.core.AlfrescoFileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.MavenLaunchHelper;
import org.eclipse.alfresco.publisher.ui.OperationCanceledException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AlfrescoDeploy implements IObjectActionDelegate {

	private static final int THREAD_SLEEP_1000 = 1000;

	private static final int DEFAULT_WAR_BACKUP_FILE_KEEP_4 = 4;

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

		try {
			new ProgressMonitorDialog(shell).run(true, true,
					new AMPDeployRunnable(this, alfrescoFileUtils, project,
							preferences,
							shoulDeactivateIncrementalDeployement()
									&& preferences.isIncrementalDeploy()));
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getLocalizedMessage(), e.getCause());
		} catch (InterruptedException e) {
			LOGGER.error(e.getLocalizedMessage(), e.getCause());
		}

	}

	protected void cleanBackupFile(AlfrescoPreferenceHelper preferences,
			IProgressMonitor monitor) {
		File webappPath = new File(preferences.getWebappAbsolutePath());
		File webappsPath = webappPath.getParentFile();

		String webappName = preferences.getWebappName();

		final Pattern pattern = Pattern.compile(webappName + ".war-(\\d+).bak");

		File[] list = webappsPath.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {

				return pattern.matcher(name).matches();
			}
		});
		List<File> backUpFiles = new ArrayList<File>();
		Collections.addAll(backUpFiles, list);
		Collections.sort(backUpFiles, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				if (o1.lastModified() < o2.lastModified()) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		for (int i = DEFAULT_WAR_BACKUP_FILE_KEEP_4; i < backUpFiles.size(); i++) {
			File file = backUpFiles.get(i);
			boolean delete = file.delete();
			if (delete) {
				LOGGER.info("Removed: " + file.getName());
			} else {
				LOGGER.warn("Could not delete: " + file.getAbsolutePath());
			}

		}

	}

	void startServer(AlfrescoPreferenceHelper preferences) throws IOException {
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
			throw new OperationCanceledException(e.getLocalizedMessage(), e);
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

	void build(IProject project, final IProgressMonitor monitor) {

		String goals = getGoals();

		final ILaunchConfiguration launchConf = MavenLaunchHelper
				.createLaunchConfiguration(project, goals);

		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				DebugUITools.launch(launchConf, ILaunchManager.RUN_MODE);

			}
		});

		IProcess currentProcess = DebugUITools.getCurrentProcess();

		while (!currentProcess.isTerminated()) {
			try {
				Thread.sleep(THREAD_SLEEP_1000);
			} catch (InterruptedException e) {
				throw new OperationCanceledException(e.getLocalizedMessage(), e);
			}
			if (monitor.isCanceled()) {

				try {
					currentProcess.terminate();
				} catch (DebugException e) {
					throw new OperationCanceledException(e.getLocalizedMessage(), e);
				}

			}
		}

	}

	protected abstract boolean shoulDeactivateIncrementalDeployement();

	protected abstract String getGoals();

	void stopServer(AlfrescoPreferenceHelper preferences) throws IOException {

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
