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
	

	public WebappDeployer(String webappPath, String ampRelativePath,
			Properties fileMapping, PrintWriter printWriter) {
		super(printWriter);
		this.webappPath = webappPath;
		this.ampRelativePath = ampRelativePath;
		this.fileMapping = fileMapping;
	
	}

	@Override
	public ResourceCommand getFile(IResource resource) {
		ResourceCommand file = super.getFile(resource);
		if (file == null) {
			file = getDeployedResource(resource);
		}
		return file;
	}

	private ResourceCommand getDeployedResource(IResource resource) {
		String path = resource.getProjectRelativePath().toString();

		if (path.startsWith(ampRelativePath)) {
			ResourceCommand resourceCommand = new ResourceCommand();
			resourceCommand.setType("RES");
			resourceCommand.setResource(resource);
			resourceCommand.setSrc(resource.getLocation().toFile());
			System.out.println(path);
			path = path.substring(ampRelativePath.length());
			resourceCommand.setSrcLog(path);
			for (Entry<Object, Object> entry : fileMapping.entrySet()) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				if (path.startsWith(key)) {
					path = path.replaceFirst(key, value);
					System.out.println(path);
					break;
				}
			}
			resourceCommand.setDst(new File(webappPath, path));
			return resourceCommand; 
		}
		return null;
	}

	@Override
	public File getClasses() {

		return new File(webappPath, "WEB-INF/classes");
	}

}
