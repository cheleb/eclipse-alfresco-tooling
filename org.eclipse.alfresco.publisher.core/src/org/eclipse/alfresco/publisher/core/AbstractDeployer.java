package org.eclipse.alfresco.publisher.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDeployer implements Deployer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractDeployer.class);
	private PrintWriter logPrinter;
	private boolean dateLogged;

	private Map<String, ResourceCommand> delayedJarResourceCommands = new HashMap<String, ResourceCommand>();

	private boolean ignoreClasses;
	private AlfrescoFileUtils fileHelper;

	public AbstractDeployer(AlfrescoFileUtils fileHelper, boolean ignoreClasses,
			PrintWriter printWriter) {
		this.fileHelper = fileHelper;
		this.logPrinter = printWriter;
	}

	@Override
	public Map<String, ResourceCommand> getDelayedJarResourceCommands() {
		return delayedJarResourceCommands;
	}

	protected ResourceCommand getDeployedClassPathResource(IResource resource,
			int kind) {

		ResourceCommand resourceCommand = buildResourceCommand(resource, kind);

		if (resourceCommand == null) {
			return null;
		}

		String pathRelativeToClasses = getPathRelativeToClasses(resource
				.getProjectRelativePath().toString());
		resourceCommand.setSrcRelative(pathRelativeToClasses);

		if (resource.getName().endsWith(".class")) {
			if (ignoreClasses) {
				return null;
			}

		}

		resourceCommand.setType("CLS");

		File file = new File(getClasses(), getPathRelativeToClasses(resource
				.getProjectRelativePath().toString()));

		resourceCommand.setDst(file);

		return resourceCommand;
	}

	protected ResourceCommand buildResourceCommand(IResource resource, int kind) {
		ResourceCommand resourceCommand = new ResourceCommand();
		resourceCommand.setResource(resource);
		resourceCommand.setSrc(resource.getLocation().toFile());

		switch (kind) {
		case IResourceDelta.ADDED:
			resourceCommand.setAction(kind);
			break;
		case IResourceDelta.REMOVED:
			resourceCommand.setAction(kind);
			break;
		case IResourceDelta.CHANGED:
			resourceCommand.setAction(kind);
			break;
		default:
			return null;
		}
		return resourceCommand;
	}

	protected boolean classPathResource(IResource resource) {

		return resource.getProjectRelativePath().toString()
				.startsWith("target/classes/");
	}

	public String getPathRelativeToClasses(String projectRelativePath) {
		String ret;
		int i = projectRelativePath.indexOf("target/classes/");
		if (i == 0) {
			ret = projectRelativePath.substring("target/classes/".length());
		} else {
			throw new RuntimeException("Not classpath: " + projectRelativePath);
		}
		return ret;
	}

	@Override
	public ResourceCommand getResourceCommand(IResource resource, int kind) {

		if (classPathResource(resource)) {

			return getDeployedClassPathResource(resource, kind);
		}
		return null;
	}

	public void addResource(ResourceCommand toCreate) {
		if ("JAR".equals(toCreate.getType())) {
			delayedJarResourceCommands.put(toCreate.getSrcRelative(), toCreate);
		} else {
			switch (toCreate.getResource().getType()) {
			case IResource.FOLDER:
				toCreate.getDst().mkdir();
				break;
			case IResource.FILE:
				try {
					log("CP", toCreate);
					FileUtils.copyFile(toCreate.getSrc(), toCreate.getDst());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}

	public void removeResource(ResourceCommand toRemove) {
		if ("JAR".equals(toRemove.getType())) {
			delayedJarResourceCommands.put(toRemove.getSrcRelative(), toRemove);
		} else {

			try {
				log("RM", toRemove);
				fileHelper.rm(toRemove.getDst());

			} catch (IOException e) {
				LOGGER.error("Could not delete dir: " + toRemove.getDst(), e);

				MessageDialog.openError(Display.getDefault().getActiveShell(),
						"Oullla", "Error : " + e.getLocalizedMessage());
				LOGGER.error("Error while deleting: " + toRemove.getDst(), e);
			}
		}

	}

	private void log(String what, ResourceCommand command) {
		if (!dateLogged) {
			dateLogged = true;
			logPrinter.append("-------- ").append(new Date().toString())
					.append(" ------- \n");
		}
		logPrinter.append(what).append(" ");
		logPrinter.append(command.getType()).append(" ");
		logPrinter.append(command.getSrcRelative());
		logPrinter.append(" -> " + command.getDst());
		logPrinter.append("\n");
	}

	void copyResource(ResourceCommand c) {
		try {
			log("CP", c);
			File parent = c.getDst().getParentFile();
			if (parent.mkdirs()) {
				LOGGER.debug("mkdir " + c.getDst());
			}
			FileUtils.copyFile(c.getSrc(), c.getDst());
		} catch (IOException e) {
			LOGGER.error("Fail deploying: ", e);
		}

	}

	public void updateResource(ResourceCommand toUpdate) {
		if ("JAR".equals(toUpdate.getType())) {
			delayedJarResourceCommands.put(toUpdate.getSrcRelative(), toUpdate);
		} else {
			if (toUpdate.getDst().exists()) {
				if (toUpdate.getDst().isFile()) {
					copyResource(toUpdate);
				}
			} else {
				LOGGER.debug("Could update (not find): "
						+ toUpdate.getDst().getAbsolutePath() + " ("
						+ toUpdate.getDst().getAbsolutePath() + ")");
				if (toUpdate.getDst().isFile()) {
					copyResource(toUpdate);
				}
			}
		}
	}

	protected abstract File getLibFolder();
}
