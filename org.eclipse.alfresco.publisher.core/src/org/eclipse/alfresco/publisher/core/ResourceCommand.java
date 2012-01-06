package org.eclipse.alfresco.publisher.core;

import java.io.File;

import org.eclipse.core.resources.IResource;

public class ResourceCommand {

	public String type;
	private File src;
	private File dst;

	private IResource resource;

	private String srcRelative;

	private String dstLog;
	private int action;

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

	public String getSrcRelative() {
		return srcRelative;
	}

	public void setSrcRelative(String srcLog) {
		this.srcRelative = srcLog;
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

	public void setAction(int action) {
		this.action = action;
	}

	public int getAction() {
		return action;
	}

	@Override
	public String toString() {
		return getType() + " " + getAction() + " " + getSrcRelative();
	}
}
