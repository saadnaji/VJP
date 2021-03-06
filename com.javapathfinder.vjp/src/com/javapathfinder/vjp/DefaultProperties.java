/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp;

import gov.nasa.jpf.Config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
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
 * @author Saad Naji
 */
public class DefaultProperties {
  
  private static final Path ENV_JPF_PATH = new Path("lib" + File.separatorChar 
                                                    + "jpf.jar");
  private static final Path JPF_CORE_PATH = new Path("");
  
  private static final Path JPF_JAR_PATH = new Path("lib" + File.separatorChar 
          											+ "jpf.jar");
  private static final Path JPF_Classes_PATH = new Path("lib" + File.separatorChar 
          											+ "jpf-classes.jar");
  private static final Path JPF_ANNOTATIONS_PATH = new Path("lib" + File.separatorChar 
          											+ "jpf-annotations.jar");

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
    
    properties.put("jpf-core", 
    		getJPFCorePropertyEntry(project,JPF_CORE_PATH,"jpf-core" ));
    properties.put("jpf-core.classpath",
    		getJPFCorePropertyEntry(project,JPF_Classes_PATH,"jpf-core.classpath" ));
    properties.put("jpf-core.native_classpath",
    		getJPFCorePropertyEntry(project,JPF_JAR_PATH,"jpf-core.native_classpath" )
    		+ ";" +
    		getJPFCorePropertyEntry(project,JPF_ANNOTATIONS_PATH,"jpf-core.native_classpath" )	);
    properties.put("classpath", getClasspathEntry(project));
    System.out.println("get source path" +getSourcepathEntry(project));
    
    return properties;
  }
  /**
   * Create a Single
   * @param project that used to determine the settings from
   * @return jpf-core property path
   */
  private static String getJPFCorePropertyEntry(IJavaProject project, Path path, String property) {
	  StringBuilder cp = new StringBuilder();
	  String jpf_core_property = null;
      URL url = FileLocator.find(VJP.getDefault().getBundle(), path, null);
      try {
    	  jpf_core_property = FileLocator.toFileURL(url).getPath();
      }catch (IOException ioe) {
    	  VJP.logError("Could not append"+ property +"path to " + property, ioe);
      }
           	
      if(jpf_core_property != null) {
    	  if(property.equals("jpf-core")) {
    		  jpf_core_property = jpf_core_property.replaceFirst(".$", "");
    	  }
      } 
    	  cp.append(jpf_core_property);
     

	  return cp.toString();
  }
  
  /**
   * 
   * @param project
   * @return String
   */
  private static String getJPFConfigEntry(IJavaProject project){
	  StringBuilder cp = new StringBuilder();
	  
	  String config = null;
	  final Path bin = new Path("src/");  
	  IProject JPFProject = project.getProject();
      URL url = FileLocator.find(VJP.getDefault().getBundle(), bin, null);
      try {
    	  config = FileLocator.toFileURL(url).getPath();
      }catch (IOException ioe) {
    	  VJP.logError("Could not append jpf path", ioe);
      }
	
	  return "";
	  
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
//    String env_jarPath = null;
//    
//    //Find and append jpf-classed.jar
//    String jpf_classes = null;
//    
//    try{
     // URL url = FileLocator.find(VJP.getDefault().getBundle(), ENV_JPF_PATH, null);
//      try {
//		System.out.println("url is" + FileLocator.toFileURL(url).getPath());
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//      env_jarPath = FileLocator.toFileURL(url).getFile();
//    }catch(IOException ioe){
//      VJP.logError("Could not append env_jpf.jar to vm.classpath", ioe);
//    }
//    
//    try{
//        URL url2 = FileLocator.find(VJP.getDefault().getBundle(), JPF_Classes_PATH, null);
//        jpf_classes = FileLocator.toFileURL(url2).getFile();
//      }catch(IOException ioe){
//        VJP.logError("Could not append jpf-classes.jar to vm.classpath", ioe);
//      }
//      
//    // add the env_jpf.jar 
//    if (env_jarPath != null){
//      cp.append(env_jarPath);    
//    }
//    
//    if (jpf_classes != null){
//        cp.append(","+jpf_classes);    
//      }
    
    // add target project paths
    appendProjectClassPaths(project,cp);
   
    return cp.toString();
  }

  /**
   * append all relevant paths from the target project settings to the vm.classpath 
   */
  private static void appendProjectClassPaths(IJavaProject project, StringBuilder cp){
    try {
      // we need to maintain order
      LinkedHashSet<IPath> paths = new LinkedHashSet<IPath>();

      // append the default output folder
      IPath defOutputFolder = project.getOutputLocation();
      if (defOutputFolder != null) {
        paths.add(defOutputFolder);
      }
      // look for libraries and source root specific output folders
      for (IClasspathEntry e : project.getResolvedClasspath(true)) {
        IPath ePath = null;

        switch ( e.getContentKind()) {
        case IClasspathEntry.CPE_LIBRARY: 
          ePath = e.getPath(); break;
        case IClasspathEntry.CPE_SOURCE:
          ePath = e.getOutputLocation(); break;
        }

        if (ePath != null && !paths.contains(ePath)) {

          paths.add(ePath);
        }
      }
      
      for (IPath path : paths) {
        String absPath = getAbsolutePath(project, path).toOSString();


//        if (cp.length() > 0) {
//          cp.append(Config.LIST_SEPARATOR);
//        }
        // only add classes that are found in bin
        if(absPath.contains("/bin"))
        	cp.append(absPath);
      }
      System.out.println("cp is" + cp);
    } catch(JavaModelException jme){
      VJP.logError("Could not append Project classpath", jme);
    }
  }
  
  private static String getSourcepathEntry(IJavaProject project){
    
    
    StringBuilder sourcepath = new StringBuilder();
    IClasspathEntry[] paths;
    
    try {
      paths = project.getResolvedClasspath(true);
    } catch (JavaModelException e) {
      VJP.logError("Could not retrieve project classpaths.",e);
      return "";
    }
    
    for(IClasspathEntry entry : paths){
      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE){  
        sourcepath.append(getAbsolutePath(project, entry.getPath()));
        sourcepath.append(Config.LIST_SEPARATOR);
      }else if (entry.getSourceAttachmentPath() != null){
        IPath path = entry.getSourceAttachmentPath();
        if (path.getFileExtension() == null){ //null for a directory
          sourcepath.append(path);
          sourcepath.append(Config.LIST_SEPARATOR);
        }
      }
    }
    if (sourcepath.length() > 0)
      sourcepath.setLength(sourcepath.length() - 1); //remove that trailing separator
   // VJP.logInfo(sourcepath.toString());
    return sourcepath.toString();
  }
  
  private static IPath getAbsolutePath(IJavaProject project, IPath relative){
    IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
    path = path.append(relative);
    return path;
  }
  
}
