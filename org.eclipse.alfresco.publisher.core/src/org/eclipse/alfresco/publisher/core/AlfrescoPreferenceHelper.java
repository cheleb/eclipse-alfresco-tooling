package org.eclipse.alfresco.publisher.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.osgi.service.prefs.Preferences;

public class AlfrescoPreferenceHelper {

	public static final String AMP_RELATIVE_PATH = "amp.relative.path";
	public static final String WEBAPP_ABSOLUTE_PATH = "webapp.absolute.path";
	public static final String SHARED_ABSOLUTE_PATH = "shared.absolute.path";

	public static Preferences getProjectPreferences(IProject project) {
		Preferences preferences = Platform
				.getPreferencesService()
				.getRootNode()
				.node("project/" + project.getName() + "/"
						+ "org.eclipse.alfresco.publisher.core");
		return preferences;
	}
}
