/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.eclipse.alfresco.publisher.ui.properties;

import org.eclipse.alfresco.publisher.ui.BooleanCellEditor;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Composite;

/**
 * see Snippet013TableViewerEditing
 */
public class InlineBooleanEditingSupport extends ObservableValueEditingSupport {
  private CellEditor cellEditor;
  private String propName;

  public InlineBooleanEditingSupport(ColumnViewer viewer, DataBindingContext dbc, String propName) {
    super(viewer, dbc);
    cellEditor = new BooleanCellEditor((Composite) viewer.getControl());
    this.propName = propName;
  }

  protected CellEditor getCellEditor(Object element) {
    return cellEditor;
  }

  protected IObservableValue doCreateCellEditorObservable(CellEditor cellEditor) {
    return SWTObservables.observeSelection(cellEditor.getControl());
  }

  protected IObservableValue doCreateElementObservable(Object element, ViewerCell cell) {
    return BeansObservables.observeValue(element, propName);
  }
  
  
}
