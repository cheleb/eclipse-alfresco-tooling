package org.eclipse.alfresco.publisher.core;

import java.io.File;
import java.io.PrintWriter;

public class SharedDeployer extends AbstractDeployer {

	private final String sharedPath;

	/**
	 * Constructor.
	 * 
	 * @param sharedPath
	 * @param ampLibFileName
	 * @param printWriter
	 * @param targetDir
	 */
	public SharedDeployer(AlfrescoFileUtils fileUtils, String sharedPath,
			boolean ignoreClasses, PrintWriter printWriter, String targetDir) {
		super(fileUtils, ignoreClasses, printWriter, targetDir);
		this.sharedPath = sharedPath;
	}

	@Override
	public File getClasses() {

		return new File(sharedPath, "classes");
	}

	@Override
	public String getRoot() {
		return sharedPath;
	}

	@Override
	protected File getLibFolder() {
		return new File(sharedPath, "lib");
	}

}
