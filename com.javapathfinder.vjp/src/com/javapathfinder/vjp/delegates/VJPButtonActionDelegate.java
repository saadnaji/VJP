/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.delegates;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.javapathfinder.vjp.config.LaunchDialog;

/**
 * This class is called by the plugin manifest to be activated when the
 * verify button on the workspace dialog is clicked.
 * @author Sandro Badame
 * @author Saad Naji
 */
public class VJPButtonActionDelegate implements IWorkbenchWindowActionDelegate {
  
  /**
   * Performs the task of opening the LaunchDialog.
   * (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    run();
  }
  
  public void run(){
    Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    LaunchDialog dialog = new LaunchDialog(parentShell);
    dialog.open(); 
  }

  public void selectionChanged(IAction action, ISelection selection) {

  }
  
  public void dispose() {

  }

  public void init(IWorkbenchWindow window) {

  }

}
