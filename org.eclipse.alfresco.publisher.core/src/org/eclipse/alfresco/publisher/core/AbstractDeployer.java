package org.eclipse.alfresco.publisher.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.core.resources.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDeployer implements Deployer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractDeployer.class);
	private PrintWriter logPrinter;

	public AbstractDeployer(PrintWriter printWriter) {
		this.logPrinter = printWriter;
	}

	protected ResourceCommand getDeployedClassPathResource(IResource resource) {

		ResourceCommand resourceCommand = new ResourceCommand();
		resourceCommand.setResource(resource);
		resourceCommand.setType("CLS");
		resourceCommand.setSrc(resource.getLocation().toFile());
		String pathRelativeToClasses = getPathRelativeToClasses(resource
				.getProjectRelativePath().toString());
		resourceCommand.setSrcLog(pathRelativeToClasses);

		File file = new File(getClasses(), getPathRelativeToClasses(resource
				.getProjectRelativePath().toString()));

		resourceCommand.setDst(file);

		return resourceCommand;
	}

	protected boolean classPathResource(IResource resource) {

		return resource.getProjectRelativePath().toString()
				.startsWith("target/classes");
	}

	public String getPathRelativeToClasses(String projectRelativePath) {
		String ret;
		int i = projectRelativePath.indexOf("target/classes");
		if (i == 0) {
			ret = projectRelativePath.substring("target/classes".length());
		} else {
			throw new RuntimeException("Not classpath: " + projectRelativePath);
		}
		return ret;
	}

	@Override
	public ResourceCommand getFile(IResource resource) {

		if (classPathResource(resource)) {

			return getDeployedClassPathResource(resource);
		}
		return null;
	}

	public void addResource(IResource resource) {
		ResourceCommand toCreate = getFile(resource);
		if (toCreate == null) {
			System.err.println("Could not deploy: " + resource);
		} else {

			switch (resource.getType()) {
			case IResource.FOLDER:
				toCreate.getDst().mkdir();
				break;
			case IResource.FILE:
				try {
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

	public void removeResource(IResource resource) {
		ResourceCommand toRemove = getFile(resource);
		if (toRemove == null) {
			System.err.println("Could not remove: " + resource);
		} else {
			if (toRemove.getDst().isDirectory()) {
				try {
					logPrinter.append("RM ");
					logPrinter.append(toRemove.getType()).append(" ");
					logPrinter.append(toRemove.getSrcLog());
					logPrinter.append(" -> ");
					logPrinter.append("\n");
					FileUtils.deleteDirectory(toRemove.getDst());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (toRemove.getDst().isFile()) {
				toRemove.getDst().delete();
			}

		}

	}

	void copyResource(ResourceCommand c) {
		try {
			logPrinter.append("CP ");
			logPrinter.append(c.getType()).append(" ");
			logPrinter.append(c.getSrcLog());
			logPrinter.append(" -> ");
			logPrinter.append("\n");
			FileUtils.copyFile(c.getSrc(), c.getDst());
		} catch (IOException e) {
			LOGGER.error("Fail deploying: ", e);
		}

	}

	public void updateResource(IResource resource) {
		ResourceCommand toUpdate = getFile(resource);
		if (toUpdate == null) {
			LOGGER.debug("Could not update: " + resource);
		} else {
			if (toUpdate.getDst().exists()) {
				if (toUpdate.getDst().isFile()) {
					copyResource(toUpdate);
				}
			} else {
				System.err.println("Could not find: "
						+ toUpdate.getDst().getAbsolutePath());
			}
			System.out.println("Copy " + toUpdate.getDst().getAbsolutePath());

		}
	}

}
