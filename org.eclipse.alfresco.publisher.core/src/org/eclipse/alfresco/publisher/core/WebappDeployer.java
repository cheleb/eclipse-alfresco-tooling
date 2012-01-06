package org.eclipse.alfresco.publisher.core;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.resources.IResource;

public class WebappDeployer extends AbstractDeployer {

	private final String webappPath;
	private String ampRelativePath;
	private Properties fileMapping;

	/**
	 * Constructor
	 * @param webappPath
	 * @param ampRelativePath
	 * @param ampLibFileName
	 * @param fileMapping
	 * @param printWriter
	 */
	public WebappDeployer(String webappPath, boolean ignoreClasses, String ampRelativePath,String ampLibFileName,
			Properties fileMapping, PrintWriter printWriter) {
		super(ampLibFileName,ignoreClasses, printWriter);
		this.webappPath = webappPath;
		this.ampRelativePath = ampRelativePath;
		this.fileMapping = fileMapping;

	}

	@Override
	public ResourceCommand getResourceCommand(IResource resource, int kind) {
		ResourceCommand file = super.getResourceCommand(resource, kind);
		if (file == null) {
			file = getDeployedResource(resource, kind);
		}
		return file;
	}

	private ResourceCommand getDeployedResource(IResource resource, int kind) {
		String path = resource.getProjectRelativePath().toString();

		if (path.startsWith(ampRelativePath)) {
			ResourceCommand resourceCommand = buildResourceCommand(resource,
					kind);
			resourceCommand.setType("RES");

			path = path.substring(ampRelativePath.length());
			resourceCommand.setSrcRelative(path);
			for (Entry<Object, Object> entry : fileMapping.entrySet()) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				if (path.startsWith(key)) {
					path = path.replaceFirst(key, value);
					break;
				}
			}
			if ("/file-mapping.properties".equals(path))
				return null;
			resourceCommand.setDst(new File(webappPath, path));
			return resourceCommand;
		}
		return null;
	}

	@Override
	public File getClasses() {

		return new File(webappPath, "WEB-INF/classes");
	}

	@Override
	public String getRoot() {
		return webappPath;
	}

	@Override
	protected File getLibFolder() {
		return new File(webappPath, "WEB-INF/lib");
	}

}
