package org.eclipse.alfresco.publisher.core.popup.actions;

public class CleanDeployAmp extends DeployAmp {

	@Override
	protected String getGoals() {
		return "clean " + super.getGoals();
	}

}
