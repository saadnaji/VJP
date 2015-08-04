package com.javapathfinder.vjp.verify.topics.link;

import org.eclipse.core.resources.IFile;

public class SourceAddress{
  public IFile file;
  public int lineNum;
  
  public SourceAddress(IFile file, int lineNum) {
    this.file = file;
    this.lineNum = lineNum;
   }
}
