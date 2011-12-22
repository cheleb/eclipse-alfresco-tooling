package org.eclipse.alfresco.publisher.core.properties;

import static org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper.SHARED_ABSOLUTE_PATH;
import static org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper.WEBAPP_ABSOLUTE_PATH;

import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.swt.widgets.Button;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class AlfrescoPropertyPage extends PropertyPage {


	private Composite composite_1;

	private Text pathValueText;
	private Label label;
	private String mode;
	private String sharedPath;
	private String webappPath;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public AlfrescoPropertyPage() {
		super();
	}

	private void addFirstSection(final Composite parent) {
		IProject project = (IProject) getElement();
		final Preferences preferences = AlfrescoPreferenceHelper
				.getProjectPreferences(project);
		mode = preferences.get("mode", "none");
		sharedPath = preferences.get(SHARED_ABSOLUTE_PATH, "");
		webappPath = preferences.get(WEBAPP_ABSOLUTE_PATH, "");
		Composite composite = createDefaultComposite(parent);
		{
			Button btnShared = new Button(composite_1, SWT.RADIO);
			btnShared.setText("Shared");
			btnShared.setSelection("Shared".equals(mode));
			btnShared.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					label.setText("Shared path: ");
					mode = "Shared";
					pathValueText.setText(preferences.get(SHARED_ABSOLUTE_PATH, ""));
					composite_1.layout();
				}
			});
		}
		{
			Button btnWebapp = new Button(composite_1, SWT.RADIO);
			btnWebapp.setText("Webapp");
			btnWebapp.setSelection("Webapp".equals(mode));
			btnWebapp.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					label.setText("Webapp path: ");
					composite_1.layout();
					mode = "Webapp";
					pathValueText.setText(preferences.get(WEBAPP_ABSOLUTE_PATH, ""));
				}
			});
		}
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);

		label = new Label(composite_1, SWT.NONE);
		label.setText("Path:");

		// Path text field
		pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		GridData gd_pathValueText = new GridData(SWT.LEFT, SWT.CENTER, true,
				false, 1, 1);
		gd_pathValueText.widthHint = 380;
		pathValueText.setLayoutData(gd_pathValueText);

		if ("Webapp".equals(mode)) {
			pathValueText.setText(webappPath);
		} else if ("Shared".equals(mode)) {
			pathValueText.setText(sharedPath);
		}

		Button btnNewButton = new Button(composite_1, SWT.NONE);
		btnNewButton.setText("...");
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				DirectoryDialog directoryDialog = new DirectoryDialog(parent
						.getShell());

				directoryDialog
						.setMessage("Please select your alfresco (tomcat) home.");

				String openDir = directoryDialog.open();

				if (openDir != null) {
					pathValueText.setText(openDir);
					if ("Webapp".equals(mode)) {
						webappPath = openDir;
					} else if ("Shared".equals(mode)) {
						sharedPath=openDir;
					}
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
		composite_1 = new Composite(parent, SWT.NULL);
		GridLayout gl_composite_1 = new GridLayout();
		gl_composite_1.numColumns = 4;
		composite_1.setLayout(gl_composite_1);

		GridData gd_composite_1 = new GridData();
		gd_composite_1.verticalAlignment = GridData.FILL;
		gd_composite_1.horizontalAlignment = GridData.FILL;
		composite_1.setLayoutData(gd_composite_1);

		return composite_1;
	}

	protected void performDefaults() {
		super.performDefaults();
		if ("Webapp".equals(mode)) {
			pathValueText.setText(webappPath);
			
		} else if ("Shared".equals(mode)) {
			pathValueText.setText(sharedPath);
		}
	}

	public boolean performOk() {
		// store the value in the owner text field
		try {
			IProject project = (IProject) getElement();
			Preferences preferences = AlfrescoPreferenceHelper
					.getProjectPreferences(project);

			preferences.put("mode", mode);
			preferences.put(WEBAPP_ABSOLUTE_PATH, webappPath);
			preferences.put(SHARED_ABSOLUTE_PATH, sharedPath);

			preferences.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}