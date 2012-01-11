package org.eclipse.alfresco.publisher.core.properties;

import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class AlfrescoPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	public AlfrescoPropertyPage() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Control createContents(Composite parent) {
		IProject project = (IProject) getElement();

		final AlfrescoPreferenceHelper pref = new AlfrescoPreferenceHelper(
				project);
		if (pref.getTargetAmpLocation() == null) {
			setErrorMessage("Maven update project configuration must be run on project.");

		}

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		doCreateContents(composite, pref);

		return composite;
	}

	public Control doCreateContents(Composite composite,
			AlfrescoPreferenceHelper pref) {

		return composite;
	}

}
