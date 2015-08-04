/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors.userdefined;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.javapathfinder.vjp.VJP;
import com.javapathfinder.vjp.config.editors.ModePropertyConfiguration;
import com.javapathfinder.vjp.config.editors.Property;
import com.javapathfinder.vjp.config.editors.PropertyViewer;

/**
 * Displays the user defined properties to be saved and modified.
 * @author Sandro Badame
 */
public class UserDefinedPropertiesViewer extends PropertyViewer {
  private String[] HEADERS = new String[] { "Name", "Value" };
  private ModePropertyConfiguration properties;

  /**
   * Constructs this PropertyViewer
   * 
   * @param parent the parent composite
   * @param properties the properties that this viewer will modify
   */
  public UserDefinedPropertiesViewer(Composite parent, ModePropertyConfiguration properties) {
    super(parent, properties);
    this.properties = properties;

    CellEditor[] editors = new CellEditor[] { new TextCellEditor(getTable()),
                                              new TextCellEditor(getTable()) };
    setCellEditors(editors);

    setColumnProperties(HEADERS);
    setContentProvider(new CustomPropertyContentProvider());
    setCellModifier(new CustomPropertyModifier());
    setLabelProvider(new CustomPropertyLabelProvider());
    setSorter(new PropertySorter());

    TableColumn nameColumn = new TableColumn(getTable(), SWT.NULL);
    TableColumn valueColumn = new TableColumn(getTable(), SWT.NULL);

    nameColumn.setText("Property");
    nameColumn.pack();
    nameColumn.setWidth(200);
    
    valueColumn.setText("Value");
    valueColumn.pack();
    valueColumn.setWidth(500);

    setInput(properties);
  }

  
  private class CustomPropertyModifier implements ICellModifier {

    // All cells can be edited
    public boolean canModify(Object element, String property) {
      return true;
    }

    public Object getValue(Object element, String column) {
      if (column.equals(HEADERS[0]))
        return ((Property) element).getName();
      else
        return ((Property) element).getValue();
    }

    public void modify(Object element, String column, Object value) {
      Property property = (Property) (((TableItem)element).getData());
      String newValue = (String) value;
      if (column.equals(HEADERS[0])) {
        if (properties.isDefaultProperty(newValue)) {
          new MessageDialog(getControl().getShell(),
                            "Property is already defined.",
                            null,
                            "The property '"+property.getName()+"' is"+
                            "already defined in the default properties."+
                            " Please change its value there.",
                            MessageDialog.WARNING,
                            new String[]{"OK"},
                            0).open();
        } else {
          properties.renameProperty(property.getName(), newValue);
          property.setName(newValue);
        }
      } else {
        properties.setProperty(property, newValue);
      }
      refresh();
    }
    

  }

  private class CustomPropertyLabelProvider implements ITableLabelProvider {

    public String getColumnText(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return ((Property) element).getName();
      } else {
        return ((Property) element).getValue();
      }
    }

    public Image getColumnImage(Object element, int columnIndex) {return null;}
    public void addListener(ILabelProviderListener listener) {}
    public void dispose() {}
    public boolean isLabelProperty(Object element, String property) {return false;}
    public void removeListener(ILabelProviderListener listener) {}
  }

  private class CustomPropertyContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object inputElement) {
      return properties.getUserDefinedPropertiesAsArray();
    }

    public void dispose() {}

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      if (newInput instanceof ModePropertyConfiguration)
        properties = (ModePropertyConfiguration) newInput;
      else if (newInput != null)
        VJP.logError("Input changed to: " + newInput);
      repackColumns();
    }

  }
}
