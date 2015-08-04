/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors;

/*
 * This class is used to help display and store properties in the tables
 */
//TODO look into whether this class is actually really needed or not.
public class Property {
  private String name, value;

  /**
   * Constructs a property with the specified name and value
   * @param name the of this property
   * @param value the value for this property
   */
  public Property(String name, String value) {
    this.name = name;
    this.value = value;

  }

  /**
   * The name of this property.
   * @return the name of this property
   */
  public String getName() {
    return name;
  }
  
  /**
   * The value of this property.
   * @return the value of this property
   */
  public String getValue() {
    return value;
  }

  /**
   * Set the name of this property
   * @param name the new name for this property
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Set the value of this property
   * @param name the new value for this property
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Returns whether the object passed is equal to this property
   */
  public boolean equals(Object o) {
    if (!(o instanceof Property))
      return false;
    Property property = (Property) o;
    if (property.getName() == null || !property.getName().equals(getName()))
      return false;
    if (property.getValue() == null || !property.getValue().equals(getValue()))
      return false;
    return true;
  }
  
  /**
   * @return a string representation of this property.
   */
  public String toString(){
    return super.toString()+"[name="+name+" ,value="+value+"]";
  }
}