/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
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
