package org.eclipse.alfresco.publisher.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class MavenLaunchHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MavenLaunchHelper.class);
	
	public static ILaunchConfiguration createLaunchConfiguration(IContainer basedir,
			String goal) {
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType launchConfigurationType = launchManager
					.getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);

			//String launchSafeGoalName = goal.replace(':', '-');

			ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType
					.newInstance(null, //
							"MyLaunch");
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR, basedir
					.getLocation().toOSString());
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, goal);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_DEBUG_OUTPUT, false);
			
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

	private static void setProjectConfiguration(
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
	private static IPath getJREContainerPath(IContainer basedir) throws CoreException {
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

}
