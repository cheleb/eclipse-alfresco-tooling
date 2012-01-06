package org.eclipse.alfresco.publisher.core.popup.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.actions.ExecutePomAction;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AlfrescoDeploy implements IObjectActionDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoDeploy.class);

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

		final AlfrescoPreferenceHelper preferences = new AlfrescoPreferenceHelper(project);

		
		

		

		IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Reloading", 4);

				try {
					monitor.subTask("Stopping server");
					stopServer(preferences);
					monitor.worked(1);
					monitor.subTask("Invoking build");
					build(project, monitor);
					monitor.worked(1);
					monitor.subTask("Deploy AMP");
					deploy(project, preferences, monitor);
					monitor.worked(1);
					monitor.subTask("Starting server");
					startServer(preferences);
					monitor.worked(1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		try {
			new ProgressMonitorDialog(shell).run(true, true,
					iRunnableWithProgress);
		} catch (InvocationTargetException e) {
			LOGGER.error("", e);
		} catch (InterruptedException e) {
			LOGGER.error("", e);
		}

	}

	private void startServer(AlfrescoPreferenceHelper preferences)
			throws IOException {
		ProcessBuilder processBuilder = null;
		Map<String, String> environment = null;
		if (preferences.isAlfresco()) {
			processBuilder = new ProcessBuilder("scripts/ctl.sh","start");
			environment = processBuilder.environment();
			environment.put("CATALINA_PID", preferences.getServerPath() + "/temp/catalina.pid");
//			processBuilder = new ProcessBuilder("bin/startup.sh");
			//processBuilder.directory(new File(serverPath).getParentFile());
			
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

	protected abstract void deploy(IProject project, AlfrescoPreferenceHelper preferences, IProgressMonitor monitor) throws IOException;
	
	
	private void build(IProject project, final IProgressMonitor monitor) {

		String goals = getGoals();
		
		
		final ILaunchConfiguration launchConf = createLaunchConfiguration(
				project, goals);

		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				DebugUITools.launch(launchConf, ILaunchManager.RUN_MODE);
				IProcess currentProcess = DebugUITools.getCurrentProcess();

				while (!currentProcess.isTerminated()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LOGGER.error(e.getLocalizedMessage(), e);
						throw new RuntimeException(e);
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

	protected abstract String getGoals();

	private ILaunchConfiguration createLaunchConfiguration(IContainer basedir,
			String goal) {
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType launchConfigurationType = launchManager
					.getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);

			String launchSafeGoalName = goal.replace(':', '-');

			ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType
					.newInstance(null, //
							"MyLaunch");
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, basedir
					.getLocation().toOSString());
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, goal);
			workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			workingCopy.setAttribute(
					IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
			workingCopy.setAttribute(RefreshTab.ATTR_REFRESH_SCOPE,
					"${project}"); //$NON-NLS-1$
			workingCopy.setAttribute(RefreshTab.ATTR_REFRESH_RECURSIVE, true);

			setProjectConfiguration(workingCopy, basedir);

			IPath path = getJREContainerPath(basedir);
			if (path != null) {
				workingCopy
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
								path.toPortableString());
			}

			// TODO when launching Maven with debugger consider to add the
			// following property
			// -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE"

			return workingCopy;
		} catch (CoreException ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
		return null;
	}

	private void setProjectConfiguration(
			ILaunchConfigurationWorkingCopy workingCopy, IContainer basedir) {
		IMavenProjectRegistry projectManager = MavenPlugin
				.getMavenProjectRegistry();
		IFile pomFile = basedir
				.getFile(new Path(IMavenConstants.POM_FILE_NAME));
		IMavenProjectFacade projectFacade = projectManager.create(pomFile,
				false, new NullProgressMonitor());
		if (projectFacade != null) {
			ResolverConfiguration configuration = projectFacade
					.getResolverConfiguration();

			String activeProfiles = configuration.getActiveProfiles();
			if (activeProfiles != null && activeProfiles.length() > 0) {
				workingCopy.setAttribute(MavenLaunchConstants.ATTR_PROFILES,
						activeProfiles);
			}
		}
	}

	// TODO ideally it should use MavenProject, but it is faster to scan
	// IJavaProjects
	private IPath getJREContainerPath(IContainer basedir) throws CoreException {
		IProject project = basedir.getProject();
		if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
			IJavaProject javaProject = JavaCore.create(project);
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				if (JavaRuntime.JRE_CONTAINER
						.equals(entry.getPath().segment(0))) {
					return entry.getPath();
				}
			}
		}
		return null;
	}

	@SuppressWarnings("restriction")
	private void buildJar2() {
		ExecutePomAction executePomAction = new ExecutePomAction();
		executePomAction.setInitializationData(null, null, "package -Pamp");

		// executePomAction.

		executePomAction.launch(selection, ILaunchManager.RUN_MODE);

		// DebugUITools;
	}

	private void stopServer(AlfrescoPreferenceHelper preferences) throws IOException {

		ProcessBuilder processBuilder;
		if(preferences.isAlfresco()) {
			processBuilder = new ProcessBuilder("scripts/ctl.sh","stop");			
		}else {
			
			processBuilder = new ProcessBuilder("java", "-cp",
					"bin/bootstrap.jar:bin/commons-daemon.jar:bin/tomcat-juli.jar",
					"org.apache.catalina.startup.Bootstrap", "stop");
		}
		processBuilder.directory(new File(preferences.getServerPath()));
		
		Process start = processBuilder.start();
		try {
			int r = start.waitFor();
			LOGGER.info("Stopping " + (preferences.isAlfresco()?"alfresco":"share") + ": " + (r==0?"OK":"ERROR"));
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
