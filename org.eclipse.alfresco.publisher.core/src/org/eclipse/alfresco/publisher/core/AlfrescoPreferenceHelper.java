package org.eclipse.alfresco.publisher.core;

import static org.eclipse.alfresco.publisher.core.AlfrescoFileUtils.path;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class AlfrescoPreferenceHelper {

	private static final String PASSWORD = "server.reload.password";

	private static final String DEPLOYMENT_MODE_WEBAPP = "Webapp";

	private static final int TIMEOUT_30 = 30;

	private static final String ALFRESCO_HOME = "alfresco.home";

	private static final String WEBAPP_NAME = "webapp.name";

	private static final String SERVER_ABSOLUTE_PATH = "server.absolute.path";

	private static final String SERVER_URL = "server.url";

	private static final String SERVER_RELOAD_LOGIN = "server.reload.login";

	// private static final String SERVER_RELOAD_PASSWORD_KEY =
	// "server.reload.password.key";

	public static final String AMP_LIB_FILENAME = "amp.lib.filename";

	public static final String AMP_FOLDER_RELATIVE_PATH = "amp.folder.relative.path";

	public static final String AMP_LIB_DEPLOY_ABSOLUTE_PATH = "amp.lib.deploy.absolute.path";

	private static final String DEPLOYMENT_MODE = "deployment.mode";

	private static final String INCREMENTAL_DEPLOY = "incremental.deploy";

	private static final String VANILLA_WAR_ABSOLUTE_PATH = "vanilla.war.absolute.path";

	private static final String SERVER_STOP_TIMEOUT = "server.stop.timeout";

	public static final String TARGET = "target";

	private Preferences preference;

	private IProject project;

	public AlfrescoPreferenceHelper(IProject project) {
		this.project = project;
		this.preference = getProjectPreferences(project);
	}

	public String getAlfrescoHome() {
		return preference.get(ALFRESCO_HOME, "");
	}

	public String getWebappName() {
		return preference.get(WEBAPP_NAME, null);
	}

	public String getWebappAbsolutePath() {
		if (DEPLOYMENT_MODE_WEBAPP.equals(getDeploymentMode())) {
			if (getServerPath() == null) {
				return null;
			}
			if (getWebappName() == null) {
				return null;
			}
			return path(getServerPath(), "webapps", getWebappName());
		}
		return null;
	}

	public String getServerPath() {
		return preference.get(SERVER_ABSOLUTE_PATH, null);
	}

	public String getServerReloadWebscriptURL(String serverUrl, boolean alfresco) {

		if (serverUrl == null) {
			return null;
		}

		if (alfresco) {
			return serverUrl + "/service/index";
		} else {
			return serverUrl + "/page/index";
		}
	}

	public String getServerReloadWebscriptURL() {
		return getServerReloadWebscriptURL(getServerURL(), isAlfresco());

	}

	public String getServerLogin() {
		return preference.get(SERVER_RELOAD_LOGIN, null);
	}

	public String getDeploymentMode() {
		// TODO Implement return preference.get(DEPLOYMENT_MODE, null);
		return DEPLOYMENT_MODE_WEBAPP;
	}

	public String getServerURL() {

		return preference.get(SERVER_URL, null);
	}

	public static Preferences getProjectPreferences(IProject project) {
		Preferences preferences = Platform
				.getPreferencesService()
				.getRootNode()
				.node("project/" + project.getName() + "/"
						+ "org.eclipse.alfresco.publisher.core");
		return preferences;
	}

	private static boolean isSecuredStorage() {
		// TODO Auto-generated method stub
		return false;
	}

	public void storePassword(String password) throws StorageException {
		if (isSecuredStorage()) {
			ISecurePreferences root = SecurePreferencesFactory.getDefault();
			ISecurePreferences node = root.node("/org/eclipse/alfresco/"
					+ project.getName());
			node.put(PASSWORD, password, true /* encrypt */);
		} else {
			preference.put(PASSWORD, password);
		}

	}

	public String getPassword() throws StorageException {
		if (isSecuredStorage()) {
			ISecurePreferences root = SecurePreferencesFactory.getDefault();
			ISecurePreferences node = root.node("/org/eclipse/alfresco/"
					+ project.getName());
			return node.get(PASSWORD, null);
		} else {
			return preference.get(PASSWORD, null);
		}

	}

	public boolean isAlfresco() {
		return isAlfresco(getWebappName());
	}

	public boolean isAlfresco(String webappName) {
		// TODO Auto-generated method stub
		return "alfresco".equals(webappName);
	}

	public void stageDeploymentMode(String mode) {
		preference.put(DEPLOYMENT_MODE, mode);

	}

	public void stageServerPath(String serverPath) {
		preference.put(SERVER_ABSOLUTE_PATH, serverPath);

	}

	public void stageWebappName(String webappName) {
		preference.put(WEBAPP_NAME, webappName);

	}

	public void stageAlfrescoHome(String text) {
		preference.put(ALFRESCO_HOME, text);

	}

	public void stageServerURL(String text) {
		preference.put(SERVER_URL, text);

	}

	public void stageServerLogin(String text) {
		preference.put(SERVER_RELOAD_LOGIN, text);

	}

	public void flush() throws BackingStoreException {
		preference.flush();

	}

	public String getAmpJarName() {

		return preference.get(AMP_LIB_FILENAME, null);
	}

	public String getAmpJarLocation() {

		if (getTargetAmpLocation() == null) {
			return null;
		}

		if (getAmpJarName() == null) {
			return null;
		}
		return getTargetAmpLocation() + File.separator + "lib"
				+ getAmpJarName();

	}

	public String getTargetAmpLocation() {

		return preference.get(AMP_FOLDER_RELATIVE_PATH, null);
	}

	public String getAmpLib() {
		String mode = getDeploymentMode();
		if (DEPLOYMENT_MODE_WEBAPP.equals(mode)) {
			return path(getWebappAbsolutePath(), "WEB-INF", "lib");
		} else if ("Shared".equals(mode)) {
			return path(getServerPath(), "shared", "lib");
		}
		return null;
	}

	public boolean ignoreClasses() {
		// TODO Auto-generated method stub
		return true;
	}

	public void setIncrementalDeploy(boolean selection) {
		preference.putBoolean(AlfrescoPreferenceHelper.INCREMENTAL_DEPLOY,
				selection);

	}

	public boolean isIncrementalDeploy() {

		return preference.getBoolean(
				AlfrescoPreferenceHelper.INCREMENTAL_DEPLOY, false);
	}

	public String getDeploymentAbsolutePath() {
		if ("Shared".equals(getDeploymentMode())) {
			return getSharedAbsolutePath();
		}
		if (DEPLOYMENT_MODE_WEBAPP.equals(getDeploymentMode())) {
			return getWebappAbsolutePath();
		}
		return null;

	}

	private String getSharedAbsolutePath() {
		return path(getServerPath(), "shared");
	}

	public void setVanillaWarAbsolutePath(String vanillaWar) {
		preference.put(AlfrescoPreferenceHelper.VANILLA_WAR_ABSOLUTE_PATH,
				vanillaWar);

	}

	public String getVanillaWarAbsolutePath() {
		return preference.get(
				AlfrescoPreferenceHelper.VANILLA_WAR_ABSOLUTE_PATH, null);
	}

	public int getStopTimeout() {

		return preference.getInt(AlfrescoPreferenceHelper.SERVER_STOP_TIMEOUT,
				TIMEOUT_30);
	}

	public String getTargetDir() {

		return preference.get(AlfrescoPreferenceHelper.TARGET, null);
	}

}
