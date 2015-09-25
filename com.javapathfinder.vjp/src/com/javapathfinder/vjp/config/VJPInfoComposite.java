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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Displays some helpful info about how to use VJP
 * @author Sandro Badame
 * @author Saad Naji
 *
 */
public class VJPInfoComposite extends Composite {

  public VJPInfoComposite(Composite parent, int style) {
    super(parent, style);
    setLayout(new RowLayout());
    createContent(this);
  }

  //TODO make this display some helpful info on how to use VJP
  private void createContent(Composite parent) {
    Label label = new Label(parent, SWT.NULL);
    label.setText("Choose a Configuration to edit.");
  }

}
