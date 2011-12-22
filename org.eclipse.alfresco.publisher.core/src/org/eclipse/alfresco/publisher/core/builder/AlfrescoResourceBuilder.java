package org.eclipse.alfresco.publisher.core.builder;

import static org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper.AMP_RELATIVE_PATH;
import static org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper.SHARED_ABSOLUTE_PATH;
import static org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper.WEBAPP_ABSOLUTE_PATH;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.Deployer;
import org.eclipse.alfresco.publisher.core.SharedDeployer;
import org.eclipse.alfresco.publisher.core.WebappDeployer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.osgi.service.prefs.Preferences;
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
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					// handle added resource
					deployer.addResource(resource);
					break;
				case IResourceDelta.REMOVED:
					deployer.removeResource(resource);
					// FIXME should be enough return false;
					break;
				case IResourceDelta.CHANGED:
					// handle changed resource
					deployer.updateResource(resource);
					break;
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
		projects[0] = getProject();
		PrintWriter logPrinter = null;
		try {
			FileWriter fileWriter = new FileWriter(getProject()
					.getFile("target/deployed.log").getLocation().toFile(),
					true);
			logPrinter = new PrintWriter(fileWriter);

			Preferences preferences = AlfrescoPreferenceHelper
					.getProjectPreferences(getProject());
			String mode = preferences.get("mode", "none");

			Deployer deployer;
			if ("Shared".equals(mode)) {
				deployer = new SharedDeployer(preferences.get(
						SHARED_ABSOLUTE_PATH, null), logPrinter);
			} else if ("Webapp".equals(mode)) {
				Properties fileMapping = new Properties();
				String path = preferences.get(AMP_RELATIVE_PATH, null)
						+ "/file-mapping.properties";
				try {
					FileReader fileReader = new FileReader(getProject()
							.getFile(new Path(path)).getLocation().toFile());
					fileMapping.load(fileReader);
					fileReader.close();
				} catch (FileNotFoundException e) {
					throw new CoreException(Status.CANCEL_STATUS);
				} catch (IOException e) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				deployer = new WebappDeployer(preferences.get(
						WEBAPP_ABSOLUTE_PATH, null), preferences.get(
						AMP_RELATIVE_PATH, null), fileMapping, logPrinter);
			} else {
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
			}
		}

		return projects;
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
