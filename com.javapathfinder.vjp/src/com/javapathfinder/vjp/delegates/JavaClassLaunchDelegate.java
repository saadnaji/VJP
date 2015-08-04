/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.delegates;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import com.javapathfinder.vjp.VJP;
import com.javapathfinder.vjp.verify.VerifyJob;

/**
 * JavaClassLaunchDelegate is used then the 'Verify...' selection is chosen
 * from right clicking on a Java Source file. 
 * 
 * Once run the package and name of the class is determined. A JPF configuration
 * file is then created in the same directory as the source file, with the 
 * target property defined. 
 * 
 * A VerifyJob is then created and scheduled to be executed.
 * 
 * While this class does use discouraged objects (namely 
 * org.eclipse.jdt.internal.core.CompilationUnit)
 * it got the job done on time. So this definatly needs to be changed to using
 * the proper classes. 
 * 
 * @author Sandro Badame
 *
 */

public class JavaClassLaunchDelegate implements IObjectActionDelegate {
  
  private ICompilationUnit source;
  
  public void selectionChanged(IAction action, ISelection selection) {
    if ( !(selection instanceof TreeSelection) )
      return;
    TreeSelection s = (TreeSelection) selection;
    source = (ICompilationUnit) s.getFirstElement();
  }

  public void run(IAction action) {
    
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(source);
    CompilationUnit compilation = (CompilationUnit) parser.createAST(null);
    PackageDeclaration pd = compilation.getPackage(); //Returns null for the
                                                      //default package
    
    String target = (pd == null) ? "" : pd.getName() + ".";
    
    
    try {
      IFile f = (IFile)source.getCorrespondingResource();
      String targetName = getTargetName(f);
      target+=targetName;
      IProject project = f.getProject();
      IPath path = f.getProjectRelativePath().removeLastSegments(1).append(targetName+".jpf");
      IFile file = project.getFile(path);
      if (file.getLocation().toFile().createNewFile()){
        file.refreshLocal(IResource.DEPTH_INFINITE, null);
        file = project.getFile(path);
        initJPFFile(file, target);
      }
      VerifyJob.verify(file, file.getProject(), true);
    } catch (JavaModelException e) {
      VJP.logError(e.getMessage(),e);
    } catch (IOException e) {
      VJP.logError(e.getMessage(),e);     
    } catch (CoreException e) {
      VJP.logError(e.getMessage(),e);     
    } 
  }
  
  /*
   * Get the appropriate filename for the config file.
   */
  private String getTargetName(IFile f) {
    String name = f.getName();
    String extension = f.getFileExtension();
    
    if ( extension != null )
      name = name.substring(0, name.length()-extension.length()-1);
    
    return name;
  }

  private void initJPFFile(IFile file, String target) throws IOException, CoreException{
    PipedInputStream fileData = new PipedInputStream();
    PipedOutputStream propertyData = new PipedOutputStream(fileData);
    PrintStream writer = new PrintStream(propertyData);
    writer.println("#Target class");
    writer.println("target="+target);
    
    writer.flush();
    writer.close();
    file.setContents(fileData, true, true, null);
    fileData.close();
  }

  public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
  
}
