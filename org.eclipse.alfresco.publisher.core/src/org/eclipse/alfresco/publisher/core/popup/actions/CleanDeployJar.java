package org.eclipse.alfresco.publisher.core.popup.actions;

public class CleanDeployJar extends DeployJar {
	@Override
	protected String getGoals() {
		return "clean " + super.getGoals();
	}
}
