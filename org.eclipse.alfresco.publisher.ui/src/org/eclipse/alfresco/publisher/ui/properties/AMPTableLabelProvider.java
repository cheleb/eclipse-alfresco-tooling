package org.eclipse.alfresco.publisher.ui.properties;

import org.eclipse.alfresco.publisher.core.AMPFile;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class AMPTableLabelProvider extends LabelProvider implements ITableLabelProvider  {

	
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof AMPFile) {
			AMPFile ampFile = (AMPFile) element;
			if(columnIndex==0) {
				return "b";
			}if(columnIndex==1) {
				return ampFile.getModuleName();
			}
			
		}
		return "c";
	}



}
