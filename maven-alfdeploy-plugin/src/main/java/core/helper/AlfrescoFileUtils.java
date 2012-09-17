package core.helper;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

public class AlfrescoFileUtils {

	private String deploymentAbsolutePath;

	private final String targetDir;

	public AlfrescoFileUtils(String serverRoot, String webappName,
			String targetDir) {

		this.targetDir = targetDir;
		if (serverRoot == null)
			throw new AlfrescoDeployementException("Tomcat root cannot be null");
		File catalina = new File(serverRoot, "/conf/catalina.properties");
		if (catalina.exists()) {

		} else {
			throw new AlfrescoDeployementException("Tomcat catalina  \""
					+ catalina.getAbsolutePath() + " \" no found.");
		}
		this.deploymentAbsolutePath = serverRoot + File.separator + "webapps"
				+ File.separator + webappName;

	}

	public boolean rm(File toRemove) throws IOException {
		if (!toRemove.getAbsolutePath().startsWith(deploymentAbsolutePath))
			throw new AlfrescoDeployementException(
					"Not allow to delete file outsite webappRoot: "
							+ deploymentAbsolutePath);
		if (toRemove.isDirectory()) {
			FileUtils.deleteDirectory(toRemove);
			return true;
		} else if (toRemove.isFile()) {
			return toRemove.delete();
		}
		// What is it ?
		return false;

	}

	public void rm(String webappAbsolutePath) throws IOException {
		rm(new File(webappAbsolutePath));

	}

	public static String path(String... paths) {
		return StringUtils.join(paths, File.separator);
	}

	public static String pathToShellCommand(String... paths) {

		return path(paths) + (File.separator.equals("\\") ? ".bat" : ".sh");
	}

	public String getTargetDir() {
		// TODO Auto-generated method stub
		return targetDir;
	}

}
