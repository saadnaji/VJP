/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.delegates;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.javapathfinder.vjp.verify.VerifyJob;

/**
 * This class is called when Verifying is launched from a *.jpf file.
 * 
 * @author Sandro Badame
 *
 */
public class ConfigFileActionDelegate implements IObjectActionDelegate {

  private IFile file = null;

  /**
   * Creates and runs the verify job.
   */
  public void run(IAction action) {
    if (file == null)
      return;
    IJavaProject jp = JavaCore.create(file.getProject());
    VerifyJob.verify(file, jp, true);
  }

  public void selectionChanged(IAction action, ISelection selection) {
    if (!(selection instanceof TreeSelection))
      return;
    TreeSelection s = (TreeSelection) selection;
    file = (IFile) s.getFirstElement();    
  }
  
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

}
