package org.eclipse.alfresco.publisher.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.eclipse.alfresco.publisher.core.AMPFile;
import org.eclipse.alfresco.publisher.core.AlfrescoDeployementException;
import org.eclipse.alfresco.publisher.core.AlfrescoFileUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.ProjectHelper;
import org.eclipse.alfresco.publisher.core.helper.AlfrescoMMTHelper;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

public class AlfrescoDeploymentPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {
	private Text ampFileText;
	private Button incrementalDeploymentButton;
	private Composite deploymentModeComposite;

	private Button webappRadioButton;
	private Button sharedRadioButton;
	private Label lblVanillaWar;
	private Text vanillaWarText;
	private Button vanillaWarButton;

	private Table table;
	private WritableList list;
	private Button btnReloadAmpList;

	public AlfrescoDeploymentPropertyPage() {
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
		grpAmpSettings.setLayout(new GridLayout(3, false));

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
		new Label(grpAmpSettings, SWT.NONE);

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
		// webappRadioButton.setSelection("Webapp".equals(pref.getDeploymentMode()));
		webappRadioButton.setSelection(true);

		sharedRadioButton = new Button(deploymentModeComposite, SWT.RADIO);
		sharedRadioButton.setText("Shared");
		// sharedRadioButton.setSelection("Shared".equals(pref.getDeploymentMode()));
		sharedRadioButton.setSelection(false);
		sharedRadioButton.setEnabled(false);
		sharedRadioButton.setToolTipText("Not supported yet.");
		new Label(grpAmpSettings, SWT.NONE);

		Label lblAmpFile = new Label(grpAmpSettings, SWT.NONE);
		lblAmpFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblAmpFile.setText("AMP File");

		ampFileText = new Text(grpAmpSettings, SWT.BORDER);
		ampFileText.setEditable(false);
		ampFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));

		if (StringUtils.isNotBlank(pref.getTargetAmpLocation())) {
			ampFileText.setText(pref.getTargetAmpLocation() + ".amp");
		}

		CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(
				grpAmpSettings, SWT.BORDER | SWT.FULL_SELECTION);

		table = viewer.getTable();
		// table = new Table(grpAmpSettings, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createTableViewer(viewer, pref);
		
		btnReloadAmpList = new Button(grpAmpSettings, SWT.NONE);
		btnReloadAmpList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadAMP(pref);
			}
		});
		btnReloadAmpList.setText("Reload AMP List");
		new Label(grpAmpSettings, SWT.NONE);
		new Label(grpAmpSettings, SWT.NONE);

		lblVanillaWar = new Label(grpAmpSettings, SWT.NONE);
		lblVanillaWar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblVanillaWar.setText("Vanilla war");

		vanillaWarText = new Text(grpAmpSettings, SWT.BORDER);
		vanillaWarText.setEditable(false);
		vanillaWarText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		if (StringUtils.isNotBlank(pref.getVanillaWarAbsolutePath())) {
			vanillaWarText.setText(pref.getVanillaWarAbsolutePath());
		}

		vanillaWarButton = new Button(grpAmpSettings, SWT.NONE);
		vanillaWarButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog directoryDialog = new FileDialog(getShell());
				directoryDialog.setFilterExtensions(new String[] { "*.war" });
				String orig = directoryDialog.open();
				if (StringUtils.isNotBlank(orig)) {
					vanillaWarText.setText(orig);
				}
			}
		});
		vanillaWarButton.setText("...");

		return composite;
	}

	private void createTableViewer(CheckboxTableViewer viewer,
			AlfrescoPreferenceHelper pref) {

		DataBindingContext dbc = new DataBindingContext();

		viewer.getTable().setHeaderVisible(true);

		{
			TableViewerColumn moduleName = new TableViewerColumn(viewer,
					SWT.LEFT);
			moduleName.getColumn().setText("Module name");
			moduleName.getColumn().setWidth(299);
			moduleName.setEditingSupport(new InlineStringEditingSupport(viewer,
					dbc, "moduleName"));
		}

		{
			TableViewerColumn version = new TableViewerColumn(viewer, SWT.LEFT);
			version.getColumn().setText("version");
			version.getColumn().setWidth(60);
			version.setEditingSupport(new InlineStringEditingSupport(viewer,
					dbc, "version"));
		}

		{
			TableViewerColumn installDate = new TableViewerColumn(viewer,
					SWT.LEFT);
			installDate.getColumn().setText("Install date");
			installDate.getColumn().setWidth(150);
			installDate.setEditingSupport(new InlineStringEditingSupport(
					viewer, dbc, "installDate"));
		}

		
		

		list = new WritableList(new ArrayList<AMPFile>(), AMPFile.class);
		ViewerSupport.bind(
				viewer,
				list,
				BeanProperties.values(new String[] { "moduleName", "version",
						"installDate" }));

		loadAMP(pref);

	}

	protected void loadAMP(AlfrescoPreferenceHelper pref) {
		AlfrescoFileUtils alfrescoFileUtils;
		try {
			setErrorMessage(null);
			alfrescoFileUtils = new AlfrescoFileUtils(pref.getServerPath(),
					pref.getWebappName());
		} catch (AlfrescoDeployementException e) {
			setErrorMessage(e.getLocalizedMessage());
			return;
		}

		AlfrescoMMTHelper alfrescoMMTHelper = new AlfrescoMMTHelper(
				(IProject) getElement().getAdapter(IProject.class),
				alfrescoFileUtils);

		List<AMPFile> ampFiles2 = alfrescoMMTHelper.getAMPFiles();

		for (AMPFile ampFile : ampFiles2) {
			list.add(ampFile);
		}
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
		if (StringUtils.isNotBlank(vanillaWarText.getText())) {
			pref.setVanillaWarAbsolutePath(vanillaWarText.getText());
		}
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			throw new AlfrescoDeployementException(e.getLocalizedMessage());
		}
		return true;
	}
}
