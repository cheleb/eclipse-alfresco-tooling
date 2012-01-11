package org.eclipse.alfresco.publisher.core.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import org.eclipse.alfresco.publisher.core.AlfrescoFileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.Deployer;
import org.eclipse.alfresco.publisher.core.ResourceCommand;
import org.eclipse.alfresco.publisher.core.SharedDeployer;
import org.eclipse.alfresco.publisher.core.WebappDeployer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlfrescoResourceBuilder extends IncrementalProjectBuilder {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AlfrescoResourceBuilder.class);

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		private Deployer deployer;

		public SampleDeltaVisitor(Deployer deployer) {
			this.deployer = deployer;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();

			if (resource.getProjectRelativePath().toString()
					.startsWith("target/")) {

				ResourceCommand resourceCommand = deployer.getResourceCommand(
						resource, delta.getKind());
				if (resourceCommand == null) {
					LOGGER.warn("Could not resolve: " + resource);
				} else {
					switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						// handle added resource
						deployer.addResource(resourceCommand);
						break;
					case IResourceDelta.REMOVED:
						deployer.removeResource(resourceCommand);
						// FIXME should be enough return false plexus compiler
						// limitation;
						break;
					case IResourceDelta.CHANGED:
						// handle changed resource
						deployer.updateResource(resourceCommand);
						break;
					}
				}
				// return true to continue visiting children.
				return true;
			}
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		private Deployer deployer;

		public SampleResourceVisitor(Deployer deployer) {
			this.deployer = deployer;
		}

		public boolean visit(IResource resource) {
			// checkResource(resource, deployer);
			// return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = "org.eclipse.alfresco.publisher.core.alfrescoResourceBuilder";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {

		IProject projects[] = new IProject[1];
		IProject project = getProject();
		projects[0] = project;
		
		AlfrescoPreferenceHelper preferences = new AlfrescoPreferenceHelper(project);
		if(!preferences.isIncrementalDeploy()) {
			return projects;
		}
		
		PrintWriter logPrinter = null;
		IFile logFile = getProject().getFile("target/deployed.log");
		try {
			FileWriter fileWriter = new FileWriter(logFile.getLocation()
					.toFile(), true);
			logPrinter = new PrintWriter(fileWriter);

			Deployer deployer = buildDeployer(preferences, logPrinter);

			if (deployer == null) {
				return projects;
			}

			if (kind == FULL_BUILD) {
				fullBuild(deployer, monitor);
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(deployer, monitor);
				} else {
					incrementalBuild(delta, deployer, monitor);
				}
			}
		} catch (IOException e1) {
			LOGGER.error("", e1);
			throw new CoreException(Status.CANCEL_STATUS);
		} finally {
			if (logPrinter != null) {
				logPrinter.close();
				logFile.refreshLocal(IFile.DEPTH_ZERO, monitor);
			}
		}

		return projects;
	}

	private Deployer buildDeployer(AlfrescoPreferenceHelper preferences, PrintWriter logPrinter)
			throws CoreException {

		final String mode = preferences.getDeploymentMode();

		Deployer deployer;
		if ("Shared".equals(mode)) {
			deployer = buildSharedDeployer(logPrinter, preferences);
		} else if ("Webapp".equals(mode)) {
			deployer = buildWebappDeployer(logPrinter, preferences);
		} else {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Preferences not set",
							"Deployment mode root must be Shared or Webapp ( \""
									+ mode + "\" is not valid )");

				}
			});
			return null;
		}
		return deployer;
	}

	private Deployer buildWebappDeployer(PrintWriter logPrinter,
			AlfrescoPreferenceHelper preferences) throws CoreException {

		String webappName = preferences.getWebappName();

		if (webappName == null) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Preferences not set",
							"Webapp root must be defined.");

				}
			});
			return null;
		}
		String deploymentRoot = preferences.getWebappAbsolutePath();
		Properties fileMapping = new Properties();
		String path = preferences.getTargetAmpLocation()
				+ "/file-mapping.properties";

		File fileMappingFile = getProject().getFile(new Path(path))
				.getLocation().toFile();
		boolean addDefault;
		if (fileMappingFile.exists()) {
			try {
				FileReader fileReader = new FileReader(fileMappingFile);
				fileMapping.load(fileReader);
				fileReader.close();
			} catch (FileNotFoundException e) {
				throw new CoreException(Status.CANCEL_STATUS);
			} catch (IOException e) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			addDefault = "true".equals(fileMapping.getProperty(
					"include.default", "true"));
		} else {
			addDefault = true;
		}
		if (addDefault) {
			addDefaultMapping(fileMapping);
		}
		
		String ampRelativePath = preferences.getTargetAmpLocation();
		AlfrescoFileUtils fileUtils = new AlfrescoFileUtils(preferences.getServerPath(), webappName);
		return new WebappDeployer(fileUtils, deploymentRoot, preferences.ignoreClasses(), ampRelativePath,
				fileMapping, logPrinter);

	}

	private Deployer buildSharedDeployer(PrintWriter logPrinter,
			AlfrescoPreferenceHelper preferences) {
		String deployementRoot = null;
		
		if (deployementRoot == null) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(Display.getDefault()
							.getActiveShell(), "Not yet",
							"This feature \"Shared deployer\" is not yet implemented.");
					// TODO Auto-generated method stub

				}
			});
			return null;
		}
		AlfrescoFileUtils fileUtils = new AlfrescoFileUtils(preferences.getServerPath(), preferences.getWebappName());
		return new SharedDeployer(fileUtils, deployementRoot, preferences.ignoreClasses(), logPrinter);

	}

	private void addDefaultMapping(Properties fileMapping) {
		fileMapping.put("/config", "/WEB-INF/classes");
		fileMapping.put("/lib", "/WEB-INF/lib");
		fileMapping.put("/web/jsp", "/jsp");
		fileMapping.put("/web/css", "/css");
		fileMapping.put("/web/images", "/images");
		fileMapping.put("/web/scripts", "/scripts");

	}

	protected void fullBuild(Deployer deployer, final IProgressMonitor monitor)
			throws CoreException {

		try {
			getProject().accept(new SampleResourceVisitor(deployer));
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta, Deployer deployer,
			IProgressMonitor monitor) throws CoreException {

		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor(deployer));

	}
}
