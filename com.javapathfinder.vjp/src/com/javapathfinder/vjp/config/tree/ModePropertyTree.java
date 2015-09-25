/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.tree;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.javapathfinder.vjp.VJP;

/**
 * Displays the tree of JPF configuration files.
 * @author Sandro Badame
 * @author Saad Naji
 */
public class ModePropertyTree extends TreeViewer {
  
  private static final String Config_File_Extension = "jpf";
  
  /**
   * Constructs the tree to be displayed
   * @param parent the parent composite of this tree
   */
  public ModePropertyTree(Composite parent) {
    super(parent, SWT.SINGLE);
    setLabelProvider(new ModePropertyLabelProvider());
    setContentProvider(new ModePropertyContentProvider());
    updateInput();  
    expandAll();
  }
  
  /**
   * Refreshes the layout and contents of this tree
   */
  public void refresh(){
    if (getContentProvider()==null)
      setContentProvider(new ModePropertyContentProvider());
    updateInput();
    super.refresh();
    expandAll();
  }
  
  /**
   * Updates the contents of this tree
   *
   */
  public void updateInput(){
    ArrayList<TreeProject> treeProjects = new ArrayList<TreeProject>();
    IProject[] workspace_projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
  
    for (IProject p : workspace_projects){  
  
      //Expeirence tells me that closed projectes don't show up here
      //Documentation tells me that they can, so I'm just making sure.
      if (!p.exists() || !p.isOpen() || !isJavaProject(p))
        continue;
      
      ArrayList<IFile> configFiles = getModePropertyFiles(p);
      if (!configFiles.isEmpty()){
        TreeProject tp = new TreeProject();
        tp.java_project = JavaCore.create(p);
        tp.modePropertyFiles = configFiles.toArray(new IFile[configFiles.size()]);
        treeProjects.add(tp);
      }
    }
    setInput(treeProjects);
  }
  
  private boolean isJavaProject(IProject workspace_project) {
    try {
      return workspace_project.hasNature(JavaCore.NATURE_ID);
    } catch (CoreException e) {
      VJP.logError("Error thrown attempting to test for Java Nature", e);
      return false;
    }
  }
  
  private ArrayList<IFile> getModePropertyFiles(IProject project){
    ArrayList<IFile> files = new ArrayList<IFile>();
    appendConfigFiles(project, files);
    return files;
  }
  
  private void appendConfigFiles(IContainer container, ArrayList<IFile> configs){
    if (!container.exists())
      return;
    
    try{
      for(IResource r : container.members())
        if (r instanceof IContainer){
          IContainer ic = (IContainer)r;
          if (!ic.isDerived() && !ic.getResourceAttributes().isHidden())
            appendConfigFiles((IContainer)r, configs);
        }else if (r instanceof IFile){
          if (r != null && 
              r.exists() && 
              r.getFileExtension() != null && 
              r.getFileExtension().equals(Config_File_Extension)){
            configs.add((IFile)r);
          }
        }
    }catch(CoreException ce){
      VJP.logError("Members could not be found.", ce);
    }
  }
  
  private class ModePropertyLabelProvider extends LabelProvider{
      public Image getImage(Object element){
        return null;
      }
      
      public String getText(Object element){
        if(element instanceof TreeProject)
          return ((TreeProject)element).java_project.getProject().getName();
        else if (element instanceof IFile)
          return ((IFile)element).getName();
        else
          VJP.logError("None IFile child in project, element:"+element);
        return null;
      }
  }
  
  private class ModePropertyContentProvider extends ArrayContentProvider 
                                    implements ITreeContentProvider{

    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof TreeProject)
        return ((TreeProject)parentElement).modePropertyFiles;
      else
        return null;
    }

    public Object getParent(Object element) {
      return null;
    }

    public boolean hasChildren(Object element) {
     return element instanceof TreeProject;
    }
  }
  
}
