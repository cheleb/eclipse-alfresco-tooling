package org.eclipse.alfresco.publisher.core.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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

public class AlfrescoPropertyPage extends PropertyPage {

	private static final String PATH_TITLE = "Path:";
	private static final String OWNER_TITLE = "&Owner:";
	private static final String OWNER_PROPERTY = "OWNER";
	private static final String DEFAULT_OWNER = "John Doe";

	private static final int TEXT_FIELD_WIDTH = 50;
	private Composite composite_1;

	private Text pathValueText;
	
	/**
	 * Constructor for SamplePropertyPage.
	 */
	public AlfrescoPropertyPage() {
		super();
	}

	private void addFirstSection(final Composite parent) {
		Composite composite = createDefaultComposite(parent);
		
		Label label = new Label(composite_1, SWT.NONE);
		label.setText("Path:");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		// Path text field
		pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		pathValueText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		pathValueText.setText(((IResource) getElement()).getFullPath().toString());
		
		try {
			String persistentProperty = ((IResource) getElement()).getPersistentProperty(
					new QualifiedName("org.eclipse.alfresco.publisher", OWNER_PROPERTY));
			if(persistentProperty !=null) {
				pathValueText.setText(persistentProperty);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		Button btnNewButton = new Button(composite_1, SWT.NONE);
		btnNewButton.setText("...");
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			  @Override
			public void widgetSelected(SelectionEvent e) {
			
				  DirectoryDialog directoryDialog = new DirectoryDialog(parent.getShell());
				  
				  directoryDialog.setMessage("Please select your alfresco (tomcat) home.");
				  
				  String openDir = directoryDialog.open();
				  
				  if(openDir != null) {
					  pathValueText.setText(openDir);
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
		gl_composite_1.numColumns = 3;
		composite_1.setLayout(gl_composite_1);

		GridData gd_composite_1 = new GridData();
		gd_composite_1.verticalAlignment = GridData.FILL;
		gd_composite_1.horizontalAlignment = GridData.FILL;
		composite_1.setLayoutData(gd_composite_1);

		return composite_1;
	}

	protected void performDefaults() {
		super.performDefaults();
		// Populate the owner text field with the default value
		pathValueText.setText(DEFAULT_OWNER);
	}
	
	public boolean performOk() {
		// store the value in the owner text field
		try {
			((IResource) getElement()).setPersistentProperty(
				new QualifiedName("org.eclipse.alfresco.publisher", OWNER_PROPERTY),
				pathValueText.getText());
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

}