/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.verify.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * The Composite the holds all of the buttons to control verification
 * @author Sandro Badame
 * @author Saad Naji
 *
 */
public class ControlPanel extends Composite {

  public static final String runText = "Run";
  public static final String runToolTip = "Let JPF run the verification";
 
  public static final String resetText = "Reset";
  public static final String resetToolTip = "Stop this JPF run.";
  
  public static final String stepText = "Step";
  public static final String stepToolTip = "Take a single step in this verification";
  
  private Button run, step, reset;
  
  /**
   * @param parent
   * @param style
   * @see org.eclipse.swt.widgets.Composite
   */
  public ControlPanel(Composite parent, int style) {
    super(parent, style);
    setLayout(new RowLayout());
    createUI();
  }
  
  private void createUI(){
    run = new Button(this, SWT.PUSH);
    run.setText(runText);
    run.setToolTipText(runToolTip);
    
    step = new Button(this, SWT.PUSH);
    step.setText(stepText);
    step.setToolTipText(stepToolTip);
    
    reset = new Button(this, SWT.PUSH);
    reset.setText(resetText);
    reset.setToolTipText(resetToolTip);
  }

  /**
   * @return the reset
   */
  public Button getResetButton() {
    return reset;
  }

  /**
   * @return the runpause
   */
  public Button getRunButton() {
    return run;
  }

  /**
   * @return the step
   */
  public Button getStepButton() {
    return step;
  }
  
}
