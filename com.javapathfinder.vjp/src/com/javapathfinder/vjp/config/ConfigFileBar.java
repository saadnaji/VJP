/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Composite that displays the buttons above the config file tree.
 * The buttons currently include:
 * <ul>
 * <li>New - creates a new JPF Configuration File </li>
 * <li> Delete - deletes the currently selected JPF Configuration File </li>
 * </ul>
 *  
 * @author Sandro Badame
 * @author Saad Naji
 */
public class ConfigFileBar extends Composite {
  
  private Button newFile;
  private Button deleteFile;
  
  /**
   * Creates this button bar and lays out its contents.
   * @param parent The parent composite for this button bar.
   */
  public ConfigFileBar(Composite parent, int style) {
    super(parent, style);
    setLayout(new RowLayout());
    createContents(this);
  }
  
  private void createContents(Composite parent){
    newFile = new Button(parent, SWT.NULL);
    deleteFile = new Button(parent, SWT.NULL);
    
    newFile.setText("New");
    deleteFile.setText("Delete");
  }
  


  /**
   * Returns the button labeled "New"
   */
  public Button getNewFileButton() {
    return newFile;
  }
  
  /**
   * Returns the button labeled "Delete"
   */
  public Button getDeleteFileButton(){
    return deleteFile;
  }
  
  /**
   * Not implemented here.
   */
  public void widgetDefaultSelected(SelectionEvent e) {}
}
