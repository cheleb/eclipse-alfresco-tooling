package org.eclipse.alfresco.publisher.core.properties;

import org.codehaus.plexus.util.StringUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoDeployementException;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.ProjectHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.osgi.service.prefs.BackingStoreException;

public class AlfrescoDeploymentPropertyPage extends PropertyPage implements
IWorkbenchPropertyPage {
	private Text ampFileText;
	private Table table;
	private Button incrementalDeploymentButton;
	private Composite deploymentModeComposite;
	
	private Button webappRadioButton;
	private Button sharedRadioButton;

	public AlfrescoDeploymentPropertyPage() {
		// TODO Auto-generated constructor stub
	}

	
		
	@Override
	public Control createContents(Composite parent) {
		IProject project = ProjectHelper.getProject(getElement());

		final AlfrescoPreferenceHelper pref = new AlfrescoPreferenceHelper(
				project);
		if (pref.getTargetAmpLocation() == null) {
			setErrorMessage("Maven update project configuration must be run on project.");

		}

		Composite composite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));


		Group grpAmpSettings = new Group(composite, SWT.NONE);
		grpAmpSettings.setText("AMP settings");
		grpAmpSettings.setLayout(new GridLayout(2, false));

		incrementalDeploymentButton = new Button(grpAmpSettings, SWT.CHECK);
		incrementalDeploymentButton
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						deploymentModeComposite
								.setEnabled(incrementalDeploymentButton
										.getSelection());
					}
				});
		incrementalDeploymentButton.setLayoutData(new GridData(SWT.LEFT,
				SWT.CENTER, false, false, 2, 1));
		incrementalDeploymentButton.setSelection(pref.isIncrementalDeploy());
		incrementalDeploymentButton.setText("Incremental deployment");

		deploymentModeComposite = new Composite(grpAmpSettings, SWT.NONE);
		FillLayout fl_deploymentModeComposite = new FillLayout(SWT.HORIZONTAL);
		fl_deploymentModeComposite.spacing = 10;
		deploymentModeComposite.setLayout(fl_deploymentModeComposite);
		GridData gd_deploymentModeComposite = new GridData(SWT.LEFT,
				SWT.CENTER, true, false, 2, 1);
		gd_deploymentModeComposite.heightHint = 24;
		deploymentModeComposite.setLayoutData(gd_deploymentModeComposite);
		deploymentModeComposite.setEnabled(incrementalDeploymentButton
				.getSelection());

		webappRadioButton = new Button(deploymentModeComposite, SWT.RADIO);
		webappRadioButton.setText("Webapp");
		//webappRadioButton.setSelection("Webapp".equals(pref.getDeploymentMode()));
		webappRadioButton.setSelection(true);

		sharedRadioButton = new Button(deploymentModeComposite, SWT.RADIO);
		sharedRadioButton.setText("Shared");
		//sharedRadioButton.setSelection("Shared".equals(pref.getDeploymentMode()));
		sharedRadioButton.setSelection(false);
		sharedRadioButton.setEnabled(false);
		sharedRadioButton.setToolTipText("Not supported yet.");

		Label lblAmpFile = new Label(grpAmpSettings, SWT.NONE);
		lblAmpFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblAmpFile.setText("AMP File");

		ampFileText = new Text(grpAmpSettings, SWT.BORDER);
		ampFileText.setEditable(false);
		ampFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		if (StringUtils.isNotBlank(pref.getTargetAmpLocation())) {
			ampFileText.setText(pref.getTargetAmpLocation() + ".amp");
		}

		table = new Table(grpAmpSettings, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		return composite;
	}

	@Override
	public boolean performOk() {
		IProject project = ProjectHelper.getProject(getElement());
		final AlfrescoPreferenceHelper pref = new AlfrescoPreferenceHelper(
				project);
		StringBuilder errorMessage = new StringBuilder();
		if (webappRadioButton.getSelection()) {
			pref.stageDeploymentMode("Webapp");
		} else if (sharedRadioButton.getSelection()) {
			pref.stageDeploymentMode("Shared");
		} else {
			errorMessage.append("Deployement mode must be choosen\n");
		}
		pref.setIncrementalDeploy(incrementalDeploymentButton.getSelection());
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			throw new AlfrescoDeployementException(e.getLocalizedMessage());
		}
		return true;
	}
}
