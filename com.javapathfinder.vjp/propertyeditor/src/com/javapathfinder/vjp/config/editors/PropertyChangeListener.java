/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors;

/**
 * Use to keep track of changes to a ModePropertyConfiguraion
 */
public interface PropertyChangeListener {
  
  /**
   * To be called when a change occurs to the property
   */
  void changeOccurred();
  
}
