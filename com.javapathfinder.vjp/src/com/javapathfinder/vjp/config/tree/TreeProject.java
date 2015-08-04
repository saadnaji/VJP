/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.tree;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

/**
 * This class represents a project for the launch tree  in the dialog.
 * It simply holds an array of configuration files and the java project that 
 * contains those configuration files.
 * 
 * @author Sandro Badame
 *
 */
//TODO is this guy really needed?
public class TreeProject{
  public IFile[] modePropertyFiles;
  public IJavaProject java_project;
}