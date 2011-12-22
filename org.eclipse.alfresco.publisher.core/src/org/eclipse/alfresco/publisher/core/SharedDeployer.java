package org.eclipse.alfresco.publisher.core;

import java.io.File;
import java.io.PrintWriter;

public class SharedDeployer extends AbstractDeployer {

	private final String sharedPath;

	public SharedDeployer(String sharedPath, PrintWriter printWriter) {
		super(printWriter);
		this.sharedPath = sharedPath;
	}

	
	@Override
	public File getClasses() {
		
		return new File(sharedPath, "classes");
	}

	

}
