package org.eclipse.alfresco.publisher.core.configurator;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlfrescoProjectConfigurator extends AbstractProjectConfigurator {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AlfrescoProjectConfigurator.class);

	public AlfrescoProjectConfigurator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void configure(ProjectConfigurationRequest configurationRequest,
			IProgressMonitor monitor) throws CoreException {
		IProject project = configurationRequest.getProject();

		MavenProject mavenProject = configurationRequest.getMavenProject();
		LOGGER.info("Configuring " + mavenProject.getName());

		Preferences projectPreferences = AlfrescoPreferenceHelper
				.getProjectPreferences(configurationRequest.getProject());

		IPath projectLocation = project.getLocation();
		String projectLocationAsString = projectLocation.toString()+"/";

		String targetDir = mavenProject.getBuild().getDirectory();

//		
		if (targetDir.startsWith(projectLocationAsString)) {
			targetDir = targetDir.substring(projectLocationAsString.length());
		} else {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		String artifactId = mavenProject.getArtifactId();
		String version = mavenProject.getVersion();

		String ampPath = String.format("%s/%s-%s", targetDir, artifactId,
				version);

		String resourceMap = getResourceMap(mavenProject);

		try {
			//projectPreferences.sync();
			projectPreferences.put("amp.relative.path", ampPath);
			projectPreferences.flush();
		} catch (BackingStoreException e) {
			LOGGER.error("Could not save preferences.", e);
		}

	}

	private String getResourceMap(MavenProject mavenProject) {
		StringBuilder builder = new StringBuilder();
		for(Resource resource: mavenProject.getResources()) {
			String targetPath = resource.getTargetPath();
			boolean filtered = resource.isFiltering();
			System.out.println(targetPath + " " + filtered);
		}
		return builder.toString();
	}

}
