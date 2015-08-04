/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.verify.view;

import gov.nasa.jpf.Error;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * Displays the errors generated from the verification
 * @author Sandro Badame
 *
 */
public class ErrorViewer extends ScrolledComposite {
  
  private ArrayList<Error> errors = new ArrayList<Error>(4); //looks like a decent number
  private Label label;
  
  public ErrorViewer(Composite parent, int style){
    super(parent, style);
    setLayout(new FillLayout());
    setExpandHorizontal(true);
    setExpandVertical(true);
    label = new Label(this, SWT.NONE);
    setContent(label);
    label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
  }
  
  /**
   * Add an error to be displayed
   * @param e the error to be added
   */
  public void addError(Error e){
    errors.add(e);
    
    Display.getDefault().syncExec(new Runnable(){
      public void run(){
        updateLabel();
        setMinSize(label.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      }
    });
  }
  
  private void updateLabel(){
    if (errors.isEmpty()){
      label.setText("No Errors Detected.");
      return;
    }
    //else...
    StringBuilder sb = new StringBuilder();
    for(Error e : errors){
      sb.append(e.getDescription());
      sb.append("\n");
      sb.append(e.getDetails());
      sb.append('\n');
    }
    label.setText(sb.toString());
  }

  /**
   * Removes all of the errors from this ErrorViewer
   */
  public void clearAll() {
    errors.clear();
    updateLabel();
  }

}


