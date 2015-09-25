/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.verify.topics.link;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.javapathfinder.vjp.VJP;

public class LinkParser implements IHyperlinkDetector{

  private static final int STACK_ELEMENT_WHITESPACE = 1;
  private static final int STACK_ELEMENT_FILE = 2;
  private static final int STACK_ELEMENT_LINK = 3;
  private static final int STACK_ELEMENT_LINE = 4;
  
  private static final int GENERIC_FILE = 1;
  private static final int GENERIC_LINE = 2;
  
  //private static final int TRANSITION_TRACE_WHITESPACE = 1;
  //private static final int TRANSITION_TRACE_LINK = 2;
  //private static final int TRANSITION_TRACE_LINE = 3;
  
  public static final Pattern STACK_ELEMENT = Pattern.compile(
            "^(\\s*)" //Get the leading whitespace
          + "at\\s"
          + "((?:\\w|\\.)+)" //Store the class name with a trailing '.'
          + "\\((\\w+\\.java:"
          + "(\\d+))" //Store the line number
          + "\\)$"
       );
  
  public static final Pattern GENERIC_JAVA =
    Pattern.compile( "([\\w" + 
                      (File.separatorChar == '\\' ? "\\\\" : File.separator ) //Special case for windows
                      + "]+\\.java):?(\\d*)");
  
  public static final Color HYPERLINK_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);

  
  private TextPresentation style;
  private ArrayList<Integer> linkboundaries;
  private HashMap<String, SourceAddress> link = new HashMap<String, SourceAddress>();
  private HashSet<String> nolink = new HashSet<String>();
  
  public LinkParser(){
    reset();
  }
  
  public void reset(){
    style = new TextPresentation();
    linkboundaries = new ArrayList<Integer>();
    linkboundaries.add(0);
  }
  
  public void parseText(String text){
    reset();
    int index = 0;
    for(String s : text.split("\n")){
      parseLine(s, index);
      index += s.length() + 1;
    }
  }
  
  public TextPresentation getTextPresentation(){
    return style;
  }
 
  private void parseLine(String line, int linestart){
    Matcher m;
    if ( (m = STACK_ELEMENT.matcher(line)).matches() ){
      parseStackElement(m, linestart);
    }else if ( (m = GENERIC_JAVA.matcher(line)).find() ){
      parseGenericLink(m, linestart);
    }
  }
  
  private void parseStackElement(Matcher m, int linestart) {
    String path = m.group(STACK_ELEMENT_FILE);
    path = path.substring(0, path.lastIndexOf('.'));
    path = path.replace('.', File.separatorChar);
    path += ".java";       

    int lineNum = Integer.parseInt(m.group(STACK_ELEMENT_LINE));
    
    int leadingspaces = m.start(STACK_ELEMENT_FILE);
    int start = linestart + m.start(STACK_ELEMENT_LINK);
    int length = m.group(STACK_ELEMENT_LINK).length();
    
    String linktext = m.group(STACK_ELEMENT_LINK);
    
    handleLink(linktext, path, lineNum, start); 
  }

  private void parseGenericLink(Matcher m, int linestart) {
    String path = m.group(GENERIC_FILE);
    int lineNum = 0;
    
    try{
      lineNum = Integer.parseInt(m.group(GENERIC_LINE));
    }catch(NumberFormatException e){
      //Probably from a link with no line number
    }
    
    int start = linestart + m.start();    
    String linktext = m.group();
    //VJP.logInfo("linktext=\""+linktext+"\" path=\""+path+"\" lineNum="+lineNum+" start="+start);
    handleLink(linktext, path, lineNum, start);
  }
  
  private void handleLink(String text, String path, int lineNum, int start){
    if ( nolink.contains(text) )
      return;//We already know that there is no link here
    
    if (link.containsKey(text) == false){//this is a potentially new link
      IFile file = findFile(path);
      if (file == null){
        nolink.add(text);
        return;
      }else{
        link.put(text, new SourceAddress(file, lineNum) );
      }
    }
   
    addBoundary(start, text.length());
  }
  
  private IFile findFile(String pathString){
    IPath path = new Path(pathString);
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    try {
      for(IProject project : projects){
        if (project.exists() && project.isOpen() && project.hasNature(JavaCore.NATURE_ID)){
          IJavaProject jproject = (IJavaProject) JavaCore.create(project);
          IJavaElement element = jproject.findElement(path);
          if (element != null){
            if (element.getResource() instanceof IFile){
              IFile file = (IFile)element.getResource();
              return file;
            }
          }
        }
      }
    } catch (CoreException e) {
      VJP.logError(e.getMessage(),e);
    }
   return null;
  }

  private void addBoundary(int start, int length){
    linkboundaries.add(start);
    linkboundaries.add(start + length);
    
    StyleRange range = new StyleRange(start, length, HYPERLINK_COLOR, null, SWT.NULL);
    range.underline = true;
    style.addStyleRange(range);
  }

  public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
    if (textViewer == null)
      return null;
    
    int index = Collections.binarySearch(linkboundaries, region.getOffset());
    if (index % 2 == 0){ //An even result means that there is no link here
      return null;
    }
    
    if (index < 0)
      index = -index - 2;
    int  offset = linkboundaries.get(index);
    int  length = linkboundaries.get(index + 1) - offset;
   
    String linktext = null;
    try {
      linktext = textViewer.getDocument().get(offset, length);
    } catch (BadLocationException e) {
      VJP.logError(e.getMessage(), e);     
    }
    
    SourceAddress s = link.get(linktext);
    return new IHyperlink[]{new SourceLink(new Region(offset, length), s)};
  }
  
}
