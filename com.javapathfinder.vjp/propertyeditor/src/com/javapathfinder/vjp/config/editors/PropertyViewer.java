/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

/*
 * Helper class to help make the userdefinedproperties table and the
 * defaultproperties table uniform.
 */
public class PropertyViewer extends TableViewer {

  protected ModePropertyConfiguration properties;

  /**
   * Constructs this PropertyViewer 
   * 
   * @param parent the parent composite for this viewer
   * @param properties the properties that this viewer will be modifying
   */
  public PropertyViewer(Composite parent, ModePropertyConfiguration properties) {
    super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
    this.properties = properties;
    getTable().setLinesVisible(true);
    getTable().setHeaderVisible(true);
    getTable().setFont(parent.getFont());
  }
  
  /**
   * Repacks all of the columns in the table.
   * This needs to be called from the SWT Thread.
   *
   */
  public void repackColumns(){
    for (TableColumn c : getTable().getColumns())
      c.pack();
  }

  /**
   * The Sorter use to sort properties.
   * (The current sort is just in alphabetical order)
   * @author sandro
   *
   */
  public class PropertySorter extends ViewerSorter {

    /*
     * Can't quite get this to work yet... public int category(Object a){
     * Property prop = (Property) a; return
     * properties.getVJPDefaults().containsKey(prop.getName()) ? 0 : 1; }
     */
    
    /**
     * Compares two properties
     */
    public int compare(Viewer viewer, Object a1, Object b1) {
      Property a = (Property) a1;
      Property b = (Property) b1;
      return a.getName().compareToIgnoreCase(b.getName());
    }
  }

}
