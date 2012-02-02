package org.eclipse.alfresco.publisher.core;

public class AMPFile extends AbstractModelObject {

	private String moduleName;
	private String title;
	private String version;
	private String installDate;
	private String description;

	public AMPFile(String filename) {
		this.moduleName = filename;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String filename) {
		firePropertyChange("moduleName", this.moduleName, filename);
		this.moduleName = filename;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		firePropertyChange("title", this.title, title);
		this.title = title;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		firePropertyChange("version", this.version, version);
		this.version = version;
	}

	public String getInstallDate() {
		return installDate;
	}
	
	public void setInstallDate(String installDate) {
		firePropertyChange("installDate", this.installDate, installDate);
		this.installDate = installDate;
		
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		firePropertyChange("description", this.description, description);
		this.description = description;
	}

	@Override
	public String toString() {
		return getModuleName();
	}
	
}
