/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * This class serves as a wrapper for the FileDialog class since Eclipse
 * doesn't allow FileDialog to be subclassed.
 * 
 * @author Sandro Badame
 * @author Saad Naji
 */
public class ModePropertyFileDialog{

  private FileDialog dialog;
  
  //The project that the config file chosen is saved in
  private IProject project = null;
  
  /**
   * @param parent the parent shell for this dialog.
   */
  public ModePropertyFileDialog(Shell parent) {
    dialog = new FileDialog(parent, SWT.SAVE);
    dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
  }
  
  /**
   * @param parent the parent shell for this dialog
   * @param properties the dialog will open with the file associated to this
   *                   property selected.
   */
  public ModePropertyFileDialog(Shell parent, ModePropertyConfiguration properties){
    this(parent);
    dialog.setFilterPath(properties.getIFile().getLocation().removeLastSegments(1).toOSString());
    dialog.setFileName(properties.getIFile().getName());
  }
  
  /**
   * Gets the file chosen from the dialog. Note that if the file chosen is not
   * contained in a poject then bad things can happen with the returned value.
   */
  public IFile getFile(){
    String spath = dialog.open();
    if (spath == null) return null;
    Path path = new Path(spath);
    project = getProjectContainingPath(path);
    if (project != null)
      return project.getFile(getPathRelativeToPath(project.getLocation(), path));
    else
      return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
  }
  
  /**
   * Returns the project that contains the chosen file
   * Null if this was saved outside of any project.
   */
  public IProject getFileProject(){
    return project;
  }
  
  private IProject getProjectContainingPath(Path path) {
    for( IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()){
      IPath location = project.getLocation();
      if(location.matchingFirstSegments(path)==location.segmentCount())
        return project;
     }
    return null;
  }
  
  private IPath getPathRelativeToPath(IPath parent, IPath target){
    int matchs = parent.matchingFirstSegments(target);
    return target.removeFirstSegments(matchs);
  }

}
