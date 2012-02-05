package org.eclipse.alfresco.publisher.ui.preferences;

import org.eclipse.alfresco.publisher.ui.AlfrescoPublisherUIActivator;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class AlfrescoPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public AlfrescoPreferencePage() {
		super(GRID);
		setPreferenceStore(AlfrescoPublisherUIActivator.getDefault().getPreferenceStore());
		setDescription("Alfresco preferences.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.ALFRESCO_PATH, 
				"&Alfresco path:", getFieldEditorParent()));
		{
			IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor(PreferenceConstants.ALFRESCO_STOP_TIMEOUT_DEFAULT, "Stop timeout", getFieldEditorParent());
			integerFieldEditor.setStringValue("30");
			integerFieldEditor.setValidRange(10, 300);
			addField(integerFieldEditor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}