package org.eclipse.alfresco.publisher.core;

import java.io.File;

import org.eclipse.core.resources.IResource;

public class ResourceCommand {
	

	private File src;
	private File dst;
	
	private IResource resource;
	
	private String srcLog;
	
	private String dstLog;
	
	public File getSrc() {
		return src;
	}

	public void setSrc(File src) {
		this.src = src;
	}

	public File getDst() {
		return dst;
	}

	public void setDst(File dst) {
		this.dst = dst;
	}

	public String getSrcLog() {
		return srcLog;
	}

	public void setSrcLog(String srcLog) {
		this.srcLog = srcLog;
	}

	public String getDstLog() {
		return dstLog;
	}

	public void setDstLog(String dstLog) {
		this.dstLog = dstLog;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public IResource getResource() {
		return resource;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	
	
	public String type;

}
