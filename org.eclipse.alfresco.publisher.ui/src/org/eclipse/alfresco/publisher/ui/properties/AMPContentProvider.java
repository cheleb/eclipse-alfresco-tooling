package org.eclipse.alfresco.publisher.ui.properties;

import org.eclipse.alfresco.publisher.core.AMPFile;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class AMPContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof AMPFile[]) {
			AMPFile[] ampFiles = (AMPFile[]) inputElement;
			return ampFiles;
		}
		return new AMPFile[0];
	}
	
	

}
