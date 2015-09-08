/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * DefaultProperties creates a set of the default VJP properties based on the 
 * IJavaProject given. The following properties are defined:
 * <ul>
 * <li>jpf.basedir - The root path to the VJP plugin</li>
 * <li>vm.classpath - The classpath defined for the project</li>
 * <li>vm.sourcepath - The sourcepath defined for the project</li>
 * </ul>
 * 
 * @author Sandro Badame
 */
public class DefaultProperties {
  
  private static final Path ENV_JPF_PATH = new Path("lib" + File.separatorChar 
                                                    + "env_jpf.jar");
  private static final Path JPF_Classes_PATH = new Path("lib" + File.separatorChar 
          + "jpf-classes.jar");

  

  /**
   * Creates a HashMap containing the VJP default key/value pairs for certain
   * properties. See the class documentation for more details on the actual
   * properties defined in this HashMap
   * @param project The project that the properties are based on
   * @return the HashMap<String, String> containing the key/value pairs of the
   *         default properties defined.
   */
  public static HashMap<String, String> getDefaultProperties(IJavaProject 
                                                                       project){
    HashMap<String, String> properties = new HashMap<String, String>();
    
    //properties.put("jpf.basedir", VJP.getRootPath());
  //  properties.put("vm.classpath", getClasspathEntry(project));
    //properties.put("vm.sourcepath", getSourcepathEntry(project));

    return properties;
  }

  /**
   * Creates a single string that can be interpretted by JPF as the claspath
   * for this project and verification.
   * @param project the project to used to determine the settings from
   * @return
   */
  private static String getClasspathEntry(IJavaProject project) {
    StringBuilder cp = new StringBuilder();
    
    //Find and append env_jpf.jar
    String env_jarPath = null;
    
    //Find and append jpf-classed.jar
    String jpf-classes = null;
    
    try{
      URL url = FileLocator.find(VJP.getDefault().getBundle(), ENV_JPF_PATH, null);
      env_jarPath = FileLocator.toFileURL(url).getFile();
    }catch(IOException ioe){
      VJP.logError("Could not append env_jpf.jar to vm.classpath", ioe);
    }
    
    try{
        URL url2 = FileLocator.find(VJP.getDefault().getBundle(), JPF_Classes_PATH, null);
        jpf-classes = FileLocator.toFileURL(url).getFile();
      }catch(IOException ioe){
        VJP.logError("Could not append jpf-classes.jar to vm.classpath", ioe);
      }
      
    //Put everything together 
    if (env_jarPath != null){
      cp.append(env_jarPath);    
    }
    
    if (jpf-classes != null){
     //   cp.append(jpf-classes);    
      }
    
    
    // Get the project output folder
    String projectFolder = getProjectOutputFolder(project);
    if (projectFolder != null){
      cp.append(File.pathSeparatorChar);
      cp.append(projectFolder);
    }
    
    //Get the project classpaths
    String classpaths = getProjectClasspaths(project);
    if (classpaths != null){
      cp.append(File.pathSeparatorChar);
      cp.append(classpaths);  
    }

    return cp.toString();
  }
  
  /*
   * Return the output location of the project folder
   */
  private static String getProjectOutputFolder(IJavaProject project){
    try{
      return getAbsolutePath(project, project.getOutputLocation()).toOSString();
    }catch(JavaModelException jme){
      VJP.logError("Project ouput location could not be found.", jme);
      return null;
    }
  }
  
  private static String getProjectClasspaths(IJavaProject project){
    try{    
      IClasspathEntry[] entries = project.getResolvedClasspath(true);
      
      if (entries.length == 0)
        return "";
      
      StringBuilder classpaths = new StringBuilder();
      classpaths.append(entries[0].getPath().makeAbsolute().toOSString());
      
      for(int i = 1; i < entries.length; i++){
        if (entries[i].getContentKind() != IPackageFragmentRoot.K_BINARY)
          continue;
        classpaths.append(File.pathSeparatorChar);
        classpaths.append(entries[i].getPath().makeAbsolute().toOSString());
      }
      
      return classpaths.toString();
      
    }catch(JavaModelException jme){
      VJP.logError("Could not append Project classpaths.", jme);
      return null;
    }
  }
  
  private static String getSourcepathEntry(IJavaProject project){
    StringBuilder string = new StringBuilder();
    IClasspathEntry[] paths;
    
    try {
      paths = project.getResolvedClasspath(true);
    } catch (JavaModelException e) {
      VJP.logError("Could not retrieve project classpaths.",e);
      return "";
    }
    
    for(IClasspathEntry entry : paths){
      if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE){
        if (string.length() != 0)
          string.append(File.pathSeparatorChar);
        string.append(getAbsolutePath(project, entry.getPath()));
      }else if (entry.getSourceAttachmentRootPath() != null){
        if (string.length() != 0)
          string.append(File.pathSeparatorChar);
        string.append(entry.getSourceAttachmentRootPath());
      }
    }
    
    return string.toString();
  }
  
  private static IPath getAbsolutePath(IJavaProject project, IPath relative){
    IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
    path = path.append(relative);
    return path;
  }
  
}
