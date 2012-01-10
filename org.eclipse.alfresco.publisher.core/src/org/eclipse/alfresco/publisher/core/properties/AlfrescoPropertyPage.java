package org.eclipse.alfresco.publisher.core.properties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.util.StringUtils;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.ServerHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

public class AlfrescoPropertyPage extends PropertyPage {

	private Composite pathComposite;

	private Text serverPathText;
	private Label lblServerPath;
	private String mode;
	private String alfrescoHome;
	private String serverPath;
	private String webappName;
	private Text serverUrlText;
	private Text login;
	private Text password;
	private Text webappNameText;
	private Button btnDeployment;

	private Group group;
	private Text alfrescoHomeText;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public AlfrescoPropertyPage() {
		super();
	}

	private void addFirstSection(final Composite parent) {
		IProject project = (IProject) getElement();
		final AlfrescoPreferenceHelper preferences = new AlfrescoPreferenceHelper(
				project);
		mode = preferences.getDeploymentMode();
		serverPath = preferences.getServerPath();
		alfrescoHome = preferences.getAlfrescoHome();
		webappName = preferences.getWebappName();

		Composite composite = createDefaultComposite(parent);
		{
			Label lblAlfrescoHome = new Label(pathComposite, SWT.NONE);
			lblAlfrescoHome.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
					false, false, 1, 1));
			lblAlfrescoHome.setText("Alfresco Home");
		}
		{
			alfrescoHomeText = new Text(pathComposite, SWT.BORDER);
			alfrescoHomeText.setText(alfrescoHome);
			alfrescoHomeText.setEditable(false);
			alfrescoHomeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
					true, false, 2, 1));
		}
		{
			Button button = new Button(pathComposite, SWT.NONE);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog directoryDialog = new DirectoryDialog(
							parent.getShell());

					directoryDialog
							.setMessage("Please select your alfresco home.");

					String openDir = directoryDialog.open();

					if (openDir != null) {
						alfrescoHomeText.setText(openDir);
					}
				}
			});
			button.setText("...");
		}
		new Label(pathComposite, SWT.NONE);
		new Label(pathComposite, SWT.NONE);
		new Label(pathComposite, SWT.NONE);
		new Label(pathComposite, SWT.NONE);
		{
			btnDeployment = new Button(pathComposite, SWT.CHECK);
			btnDeployment.setSelection(preferences.isIncrementalDeploy());
			btnDeployment.setText("Deployment");
		}
		new Label(pathComposite, SWT.NONE);
		new Label(pathComposite, SWT.NONE);
		new Label(pathComposite, SWT.NONE);
		{
			Button btnShared = new Button(pathComposite, SWT.RADIO);
			btnShared.setText("Shared");
			btnShared.setSelection("Shared".equals(mode));
			btnShared.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					mode = "Shared";
					webappNameText.setEnabled(false);
					group.setEnabled(false);
					pathComposite.layout();
				}
			});
		}
		{
			Button btnWebapp = new Button(pathComposite, SWT.RADIO);
			btnWebapp.setText("Webapp");
			btnWebapp.setSelection("Webapp".equals(mode));
			btnWebapp.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					webappNameText.setEnabled(true);
					group.setEnabled(true);
					mode = "Webapp";
					pathComposite.layout();
				}
			});
		}
		new Label(pathComposite, SWT.NONE);
		new Label(pathComposite, SWT.NONE);
		{
			Label lblWebappName = new Label(pathComposite, SWT.NONE);
			lblWebappName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
					false, false, 1, 1));
			lblWebappName.setText("Webapp name");
		}
		{
			webappNameText = new Text(pathComposite, SWT.BORDER);
			webappNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
					true, false, 2, 1));
			if (StringUtils.isNotBlank(webappName))
				webappNameText.setText(webappName);
		}
		group = new Group(pathComposite, SWT.NONE);
		{
			group.setLayout(new FillLayout(SWT.HORIZONTAL));
			GridData gd_group = new GridData(SWT.LEFT, SWT.CENTER, false,
					false, 1, 1);
			gd_group.heightHint = 47;
			gd_group.widthHint = 143;
			group.setLayoutData(gd_group);
			{
				Button btnRadioButton = new Button(group, SWT.NONE);
				btnRadioButton.setText("Alfresco");

				btnRadioButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						webappNameText.setText("alfresco");
						if (StringUtils.isEmpty(serverPathText.getText())
								&& StringUtils.isNotEmpty(alfrescoHomeText
										.getText()))
							serverPathText.setText(alfrescoHomeText.getText()
									+ File.separator + "alfresco");

					}
				});
			}
			{
				Button btnRadioButton_1 = new Button(group, SWT.NONE);
				btnRadioButton_1.setText("Share");
				btnRadioButton_1.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						webappNameText.setText("share");
					}
				});
			}
		}

		lblServerPath = new Label(pathComposite, SWT.NONE);
		lblServerPath.setText("Server Path:");

		// Path text field
		serverPathText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		GridData gd_pathValueText = new GridData(SWT.LEFT, SWT.CENTER, true,
				false, 2, 1);

		gd_pathValueText.horizontalAlignment = GridData.FILL;

		serverPathText.setLayoutData(gd_pathValueText);

		if (StringUtils.isNotBlank(serverPath)) {
			serverPathText.setText(serverPath);
		}

		Button btnNewButton = new Button(pathComposite, SWT.NONE);
		btnNewButton.setText("...");
		{
			Label lblServerUrl = new Label(pathComposite, SWT.NONE);
			lblServerUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
					false, false, 1, 1));
			lblServerUrl.setText("Server URL");
		}
		serverUrlText = new Text(pathComposite, SWT.BORDER);
		serverUrlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				3, 1));
		{
			String urlSaved = preferences.getServerURL();
			if (urlSaved != null) {
				serverUrlText.setText(urlSaved);
			}
		}

		{
			Label lblLogin = new Label(pathComposite, SWT.NONE);
			lblLogin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
					false, 1, 1));
			lblLogin.setText("Login");
		}
		login = new Text(pathComposite, SWT.BORDER);
		login.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2,
				1));
		{
			String loginSaved = preferences.getServerLogin();
			if (loginSaved != null) {
				login.setText(loginSaved);
			}
		}
		{
			final Button btnTestConnection = new Button(pathComposite, SWT.NONE);
			btnTestConnection.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 1, 2));

			btnTestConnection.setText("Test connection");
			btnTestConnection.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					String reloadURL = preferences
							.getServerReloadWebscriptURL(serverUrlText.getText(), preferences.isAlfresco(webappNameText.getText()));

					if (ServerHelper.reload(reloadURL, login.getText(),
							password.getText(), new NullProgressMonitor())) {
						MessageDialog.openInformation(getShell(), "Success",
								"Server pinged");
					} else {
						MessageDialog.openError(getShell(), "Failed",
								"Failed to contact server.");
					}
				}
			});
		}
		{
			Label lblPassword = new Label(pathComposite, SWT.NONE);
			lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
					false, false, 1, 1));
			lblPassword.setText("Password");
		}
		password = new Text(pathComposite, SWT.BORDER | SWT.PASSWORD);
		password.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		{
			try {
				String pwd = AlfrescoPreferenceHelper.getPassword(project
						.getName());
				if (StringUtils.isNotBlank(pwd)) {
					password.setText(pwd);
				}
			} catch (StorageException e1) {
				throw new RuntimeException(e1);
			}
		}

		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				DirectoryDialog directoryDialog = new DirectoryDialog(parent
						.getShell());

				directoryDialog.setMessage("Please select your tomcat home.");

				String openDir = directoryDialog.open();

				if (openDir != null) {
					serverPathText.setText(openDir);
					serverPath = openDir;
				}

			}
		});

	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSecondSection(final Composite parent) {

	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);
		addSeparator(composite);
		addSecondSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		pathComposite = new Composite(parent, SWT.NULL);
		GridLayout gl_pathComposite = new GridLayout();
		gl_pathComposite.numColumns = 4;
		pathComposite.setLayout(gl_pathComposite);

		GridData gd_pathComposite = new GridData();
		gd_pathComposite.verticalAlignment = GridData.FILL;
		gd_pathComposite.horizontalAlignment = GridData.FILL;
		pathComposite.setLayoutData(gd_pathComposite);

		return pathComposite;
	}

	protected void performDefaults() {
		super.performDefaults();

		serverPathText.setText(serverPath);

	}

	public boolean performOk() {
		// store the value in the owner text field
		StringBuilder errorMessage = new StringBuilder();
		try {
			IProject project = (IProject) getElement();
			AlfrescoPreferenceHelper preferences = new AlfrescoPreferenceHelper(
					project);
			if (StringUtils.isBlank(mode)) {
				errorMessage.append("Deployement mode must be choosen\n");
			} else {
				preferences.stageDeploymentMode(mode);
			}
			if (StringUtils.isBlank(serverPathText.getText())) {
				errorMessage.append("ServerPath must be set\n");
			} else {
				preferences.stageServerPath(serverPathText.getText());
			}
			if (StringUtils.isBlank(webappNameText.getText())) {
				errorMessage.append("Webapp name must be set.\n");
			} else {
				preferences.stageWebappName(webappNameText.getText());
			}

			String projectName = ((IProject) getElement()).getName();

			if (StringUtils.isBlank(alfrescoHomeText.getText())) {
				errorMessage.append("Alfresco Home must be set.\n");
			} else {
				preferences.stageAlfrescoHome(alfrescoHomeText.getText());
			}

			if (StringUtils.isNotBlank(serverUrlText.getText())) {
				new URL(serverUrlText.getText());
				preferences.stageServerURL(serverUrlText.getText());
			}
			if (StringUtils.isNotBlank(login.getText())) {
				preferences.stageServerLogin(login.getText());
			}
			if (StringUtils.isNotBlank(password.getText())) {
				AlfrescoPreferenceHelper.storePassword(projectName,
						password.getText());
			}

			if (errorMessage.length() > 0) {
				setErrorMessage(errorMessage.toString());
				return false;
			}

			preferences.flush();

			preferences.setIncrementalDeploy(btnDeployment.getSelection());

		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			MessageDialog.openError(getShell(), "Bad URL",
					"This is not a valid URL: " + serverUrlText.getText());
			serverUrlText.setFocus();
			return false;
		}
		return true;
	}
}