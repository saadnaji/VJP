/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors.defaultproperties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
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
 * A PropertyViewer that displays and edits all of the default properties
 * stored in jpf.properties and default.properties
 * @author Sandro Badame
 */
public class DefaultPropertiesViewer extends PropertyViewer {

  public static final String[] TABLE_HEADERS = new String[3];
  public static final String DEFAULT_HEADER = "Default";
  public static final int DEFAULT_INDEX = 0;
  public static final String NAME_HEADER = "Name";
  public static final int NAME_INDEX = 1;
  public static final String VALUE_HEADER = "Value";
  public static final int VALUE_INDEX = 2;

  // Names of images used to represent checkboxes
  public static final String CHECKED_IMAGE_PATH = "images/checked.gif";
  public static final String UNCHECKED_IMAGE_PATH = "images/unchecked.gif";
  public static final String CHECKED_IMAGE = "checked";
  public static final String UNCHECKED_IMAGE = "unchecked";
  // For the checkbox images
  private static ImageRegistry imageRegistry = new ImageRegistry();

  static {
    TABLE_HEADERS[DEFAULT_INDEX] = DEFAULT_HEADER;
    TABLE_HEADERS[NAME_INDEX] = NAME_HEADER;
    TABLE_HEADERS[VALUE_INDEX] = VALUE_HEADER;

    imageRegistry.put(CHECKED_IMAGE,
                      ImageDescriptor.createFromURL(VJP.getResourceURL(CHECKED_IMAGE_PATH)));
    imageRegistry.put(UNCHECKED_IMAGE,
                      ImageDescriptor.createFromURL(VJP.getResourceURL(UNCHECKED_IMAGE_PATH)));
  }

  /**
   * Construct a DefaultPropertiesViewer
   * @param parent the parent composite
   * @param properties the properties associated with this viewer
   */
  public DefaultPropertiesViewer(Composite parent,
                                 ModePropertyConfiguration properties) {
    super(parent, properties);

    CellEditor[] editors = new CellEditor[]{new CheckboxCellEditor(getTable()),
                                            new TextCellEditor(getTable()),
                                            new TextCellEditor(getTable()) };
    setCellEditors(editors);
    setColumnProperties(TABLE_HEADERS);

    ICellModifier modifier = new PropertyCellModifier();

    setCellModifier(modifier);
    setContentProvider(new PropertyContentProvider());
    setLabelProvider(new PropertyTableLabelProvider());
    setSorter(new PropertySorter());

    TableColumn defaultColumn = new TableColumn(getTable(), SWT.NULL);
    TableColumn nameColumn = new TableColumn(getTable(), SWT.NULL);
    TableColumn valueColumn = new TableColumn(getTable(), SWT.NULL);

    defaultColumn.setText("Use Default");
    defaultColumn.pack();

    nameColumn.setText("Property");
    nameColumn.pack();
    nameColumn.setWidth(200);
    
    valueColumn.setText("Value");
    valueColumn.pack();
    valueColumn.setWidth(500);

    setInput(properties);
  }

  /**
   * This class returns a duplicate hash of the properties contained by this
   * viewer. Modifying the hash returned will have no effect on the properties
   * held by this viewer public HashMap<String, String> getProperties() {
   * return new HashMap<String, String>(properties); }
   */
  class PropertyCellModifier implements ICellModifier {

    /**
     * @return true if this element can be modified. false if otherwise.
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
     *      java.lang.String)
     */
    public boolean canModify(Object element, String property) {
      int index = getColumnIndex(property);
      if (index == DEFAULT_INDEX)
        return true;
      if (index == NAME_INDEX)
        return false;
      String propertyName = ((Property) element).getName();
      return !properties.isUsingDefaultValue(propertyName);
    }

    /**
     * Returns the value to be displayed in a table cell
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
     *      java.lang.String)
     */
    public Object getValue(Object element, String property) {
      int index = getColumnIndex(property);
      Property prop = (Property) element;
      if (index == DEFAULT_INDEX) {
        return new Boolean(properties.isUsingDefaultValue(prop.getName()));
      } else if (index == NAME_INDEX)
        return prop.getName();
      else
        return prop.getValue();
    }

    /**
     * Actually modifies the DefaultPropertiesConfiguration
     * This method is responsible for actually changing the values in the model
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
     *      java.lang.String, java.lang.Object)
     */
    public void modify(Object element, String property, Object value) {
      int columnIndex = getColumnIndex(property);
      Property prop = (Property) ((TableItem) element).getData();
      if (columnIndex == DEFAULT_INDEX) {

        if (((Boolean) value).booleanValue())
          properties.setToDefaultValue(prop);
        else
          properties.setProperty(prop);
        
      } else if (columnIndex == VALUE_INDEX) {
        properties.setProperty(prop, (String) value);
      } else {
        VJP.logWarning("Unexpected Else reached in if statement: columnIndex="
                       + columnIndex);
      }
      refresh();
    }

    /**
     * Returns the index of the column passed. @param columnName the column whos
     * index is needed. @return Returns the index of the column specified, -1 if
     * the column does not exist.
     */
    public int getColumnIndex(String columnName) {
      int i = TABLE_HEADERS.length - 1;
      while (!TABLE_HEADERS[i].equals(columnName) && i >= 0) {
        i--;
      }
      return i;
    }

  }

  /**
   * Used to provide labels for the PropertyTable. <p>This class requires the
   * mapping of all non default properties. This is so that the class knows
   * whether or not a particular property is a default property.
   */
  private class PropertyTableLabelProvider implements ITableLabelProvider {

    /**
     * Only returns checkbox images for the first column of the table
     */
    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == DefaultPropertiesViewer.DEFAULT_INDEX) {
        String propName = ((Property) element).getName();
        String image = properties.isUsingDefaultValue(propName) ? CHECKED_IMAGE
          : UNCHECKED_IMAGE;
        return imageRegistry.get(image);
      }
      return null;
    }

    /**
     * Gives the text to be displayed.
     */
    public String getColumnText(Object element, int columnIndex) {
      Property prop = (Property) element;
      if (columnIndex == DefaultPropertiesViewer.DEFAULT_INDEX)
        return null;
      else if (columnIndex == DefaultPropertiesViewer.NAME_INDEX)
        return prop.getName();
      else
        return prop.getValue();

    }

    public void addListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }
  }

  /**
   * Provides the array filled with table items for the tableviewer
   */
  private class PropertyContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object inputElement) {
      return properties.getDefaultPropertiesAsArray();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {     
      if (newInput instanceof ModePropertyConfiguration)
        properties = (ModePropertyConfiguration) newInput;
      else if (newInput != null)
        VJP.logError("Input changed to: " + newInput);
      repackColumns();
    }

    public void dispose() {
    }
  }
}
