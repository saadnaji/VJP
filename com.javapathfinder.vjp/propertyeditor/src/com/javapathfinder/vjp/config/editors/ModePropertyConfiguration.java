/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.javapathfinder.vjp.DefaultProperties;
import com.javapathfinder.vjp.VJP;
/**
 * This class serves to manage the userdefined and default properties for JPF.
 * There are three levels of properties:
 * <ul>
 * <li>JPF default - a static set of properties loaded from jpf.properties and default.properties
 * <li>VJP default - properties determined by VJP (Overrides some properties from JPF default)
 * <li>User defined - properties determined by the user (Overrides the properties defined in VJP or JPF default)
 * </ul>
 * 
 * Each of these levels is represented by a map that contains the name/value pair
 * of their respective properties. When a property is to be retrieved this class
 * first looks to the user defined layer, then to the VJP layer and then finally
 * to the JPF layer. Values defined by the user should only be defined on the 
 * user defined layer. 
 * Only the user defined layer is saved to the Mode Property Configuration File.
 * 
 * @author Sandro Badame
 */
public class ModePropertyConfiguration{

  
  /**
   * the default properties according to jpf.defaults
   */ 
  private static HashMap<String, String> jpfDefined;
  
  /**
   * contains default properties as determined by VJP by this project.
   */
  private HashMap<String, String> vjpDefined;
  
  /**
   * contains the custom user defined values
   */
  private HashMap<String, String> userDefined = new HashMap<String, String>();
    
  /**
   * This is set true when there are changes to be made to the property file.
   */
  private boolean isDirty = false;
  
  /**
   * The file that this ModePropertyConfiguration refers to.
   */
  private IFile file;
  
  /**
   * Hangs onto any listeners that this may have
   */
  private ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>(3);
   
  /**
   * Creates a default instance of the PropertyManager. Loads values from 
   * file "jpf.properties", then loads values from the config file specified.
   * 
   * @param configFile the file that properties and loaded from and stored to.
   * @param project the project used to determine the VJP properties
   */
  public ModePropertyConfiguration(IFile configFile, IJavaProject project) {
    this.file = configFile;
    checkJPFDefaults();
    setVJPDefaults(project);
    parseConfigFile();   
  }
  
  /**
   * checks to see if the JPF defaults have been loaded.
   * If not, they are.
   */
  @SuppressWarnings("unchecked")
  private static void checkJPFDefaults() {
    if (jpfDefined != null)
      return;
    jpfDefined = new HashMap<String, String>();
    Config config = new Config(null, "jpf.properties", "", JPF.class);
    Enumeration<String> numer = (Enumeration<String>) config.propertyNames();
    while (numer.hasMoreElements()) {
      String key = numer.nextElement();
      jpfDefined.put(key, config.getProperty(key));
    }
  }
  
  /**
   * Sets the VJP default properties for this configuration based on the the
   * project given.
   * 
   * @param project The project to be used to define the VJP default settings
   */
  public void setVJPDefaults(IJavaProject project){
    vjpDefined = DefaultProperties.getDefaultProperties(project);
  }
  
  /**
   * Clears all of the config properties contained and then reloads all 
   * properties from the configuration file.
   * NOTE: this does not reload from jpf.properties or default.properties
   * 
   * @throws IOException if the file can not be read
   * @throws CoreException if the file can not be found
   */
  public void reloadFromFile() throws IOException, CoreException{
    vjpDefined.clear();
    userDefined.clear();
    parseConfigFile();
  }
  
  /**
   * Parses the configuration file and handles it appropriatly
   *
   */
  public void parseConfigFile(){
    HashMap<String, String> h = getConfigFileProperties(file);
    for(String key : h.keySet()){
      handleProperty(key, h.get(key));
    }
  }

  /**
   * Loads this configuration with the properties contained in configuration
   * file associated.
   * @throws IOException
   * @throws CoreException
   * @return the HashMap that contains all of the properties loaded from 
   *         jpf.properties and default.properties
   */
  public static HashMap<String, String> getConfigFileProperties(IFile file){
   HashMap<String, String> h = new HashMap<String, String>();
   BufferedReader reader;
   String line;
   
   try{
     reader = new BufferedReader(new InputStreamReader(file.getContents()));
   }catch(CoreException ce){
     VJP.logError("Input Stream from config file could not be opened.", ce);
     return h;
   }
   
   try{  
     while ((line = reader.readLine()) != null) {
       line = line.trim();
       if (line.equals("") || line.charAt(0) == '#')
         continue;
       int index = line.indexOf('=');
       if (index != -1){
         String name = line.substring(0, index).trim();
         String value = line.substring(index+1, line.length()).trim();
         h.put(name, value);
       }
     }
   }catch(IOException ioe){
     VJP.logError("Could not read line from config file", ioe);
   }
   try{
     if (reader !=  null)reader.close();
   }catch(IOException ioe){
     VJP.logError("Could not close InputStream.", ioe);
   }
   return h;
  }
  
  /*
   * Decides where a property that has been loaded from a file belongs.
   */
  private void handleProperty(String name, String value){
    if (!(vjpDefined.containsKey(name) && vjpDefined.get(name).equals(value)))
     userDefined.put(name, value);
  }
  
  /**
   * Saves the properties stored in this configuration to the corresponding
   * file.
   */
  public void save() throws IOException, CoreException{
    save(new NullProgressMonitor());
  }
  
  /**
   * Saves the properties stored in this configuration to the corresponding
   * file. Progress can be logged on the monitor passed.
   * 
   * @param monitor used to track the progress of saving the file
   */
  public void save(IProgressMonitor monitor) throws IOException, CoreException{
    monitor.beginTask("Saving Configuration File", 3);
    
    monitor.subTask("Reading Configuration File...");
    ArrayList<ConfigLine> contents = getFileContents(file);
    ArrayList<String> properties = new ArrayList<String>(userDefined.keySet());
    monitor.worked(1);
    
    monitor.subTask("Opening data streams...");
    PipedInputStream fileData = new PipedInputStream();
    PipedOutputStream propertyData = new PipedOutputStream(fileData);
    PrintStream writer = new PrintStream(propertyData);
    monitor.worked(1);
    
    monitor.subTask("Writing to file...");
    for(ConfigLine c : contents){
      writer.println(c.getLine());
      if (c instanceof PropertyLine)
        properties.remove(((PropertyLine)c).getPropertyName());
    }

    for(String p : properties){
      writer.println(p+"="+getPropertyValue(p));
    }

    writer.flush();
    writer.close();
    file.setContents(fileData, true, true, new SubProgressMonitor(monitor,1));
    fileData.close();
    setDirty(false);
    monitor.done();
  }
  
  private ArrayList<ConfigLine> getFileContents(IFile file) throws CoreException, IOException{
    ArrayList<ConfigLine> c = new ArrayList<ConfigLine>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));

    String s;
    while((s = reader.readLine()) != null){
      if (isPropertyLine(s)){
        s = s.trim();
        PropertyLine p = new PropertyLine(s.substring(0, s.indexOf('=')));
        c.add(p);
      }else{
        c.add(new ConfigLine(s));
      }
    }
    reader.close();
    return c;
  }
  
  private static boolean isPropertyLine(String line){
    String s = line.trim();
    return !(s.equals("") || s.charAt(0) == '#');
  }
  
  /**
   * Move the property file.
   * @param newPath the path representing the new location of the configuration file
   * @param monitor tracks the progress of moving the file to the new location
   */
  public void movePropertiesFile(IPath newPath, IProgressMonitor monitor) throws CoreException{
      file.move(newPath, false, monitor);
  }
  
  /**
   * @return true if there are properties that need to be saved to a file.
   */
  public boolean isDirty(){
    return isDirty;
  }
  
  private void setDirty(boolean isDirty){
    this.isDirty = isDirty;
    if (isDirty())
      fireListeners();
  }
  
  /**
   * Retrieves a value for the property of this name.
   * This method first looks to userDefined values, then to VJP defined values
   * (unless useVJP has been set to false), then finally to JPF defined.
   * If none of these levels define the property, then a null value is returned.
   * 
   * @return the value of the property
   */
  public String getPropertyValue(String name) {
    if (userDefined.containsKey(name)) {
      return userDefined.get(name);
    } else if (vjpDefined.containsKey(name)){
      return vjpDefined.get(name);
    } else {
      return jpfDefined.get(name);
    }
  }
  
  /**
   * @return true if the property specified is defined.
   */
  public boolean isDefined(String propertyName){
    return userDefined.containsKey(propertyName) || 
            vjpDefined.containsKey(propertyName) ||
            jpfDefined.containsKey(propertyName);
  }
  
  /**
   * @return true if the property specified is defined.
   */
  public boolean isDefined(Property prop){
    return isDefined(prop.getName());
  }
  
  /**
   * @return true if the property is user defined.
   */
  public boolean isUserDefined(String prop){
    return userDefined.containsKey(prop);
  }
  
  /**
   * Retrieves a {@link Property} object for the property of this name.
   * This method first looks to userDefined values, then to VJP defined values
   * (unless useVJP has been set to false), then finally to JPF defined.
   * If none of these levels define the property, then a null value is returned.
   * 
   * @return a property object that represents the key/value of the property
   */
  public Property getProperty(String propertyName) {
    return new Property(propertyName, getPropertyValue(propertyName));
  }
  

  /**
   * Sets the given property as a User defined property.
   * @param name the name for the property being defined
   * @param value the value for the property being defined
   */
  public void setProperty(String name, String value) {
    setDirty(true);
    userDefined.put(name, value);
  }

  /**
   * Sets the given property as a User defined property.
   * @param prop the property to set in this configuration
   */
  public void setProperty(Property prop) {
    setProperty(prop.getName(), prop.getValue());
  }

  /**
   * Sets the given property as a User defined property.
   * @param prop the name of the property to be defined
   * @param value the value of the propert being defined
   */
  public void setProperty(Property prop, String value) {
    prop.setValue(value);
    setProperty(prop);
  }
  
  /**
   * Remove the user defined value of a setting and set it back to its 
   * default value
   * @param prop the property to set back to its default value
   */
  public void setToDefaultValue(Property prop) {
    setDirty(true);
    removeProperty(prop.getName());
    prop.setValue(getPropertyValue(prop.getName()));
  }
  
  /**
   * Returns whether the property is using the default value or a user defined
   * value. If the property is not defined in either JPF or VJP then a false
   * value will be returned.
   * 
   * @param propertyName the name of the property to be checked
   * @return true if the property is using the default value
   *         false if the property is using a custom user defined value.
   */
  public boolean isUsingDefaultValue(String propertyName) {
    return !userDefined.containsKey(propertyName) 
           && isDefaultProperty(propertyName);
  }
  
  /**
   * Returns whether the property is defined by either jpfDefaults or if in use
   * VJP defaults.
   * @return true if the property is defined as a default property
   */
  public boolean isDefaultProperty(String propertyName){
    return vjpDefined.containsKey(propertyName) ||
           jpfDefined.containsKey(propertyName);
  }

  /**
   * Set the VJP default value for a property
   * 
   * @param name the name of the property to be defined.
   * @param value the value of the property to be defined.
   */
  public void setVJPDefault(String name, String value){
    setDirty(true);
    vjpDefined.put(name, value);
  }
  
  /**
   * Removes this property from this configuration.
   */
  public void removeProperty(String propertyName){
    setDirty(true);
    userDefined.remove(propertyName);
  }
  
  /**
   * Removes the property from the userdefined status. 
   * Note: only user defined properties can be removed.
   * This is the same as settings it back to its default value.
   * 
   * @param the property to be removed from this configuration
   */
  public void removeProperty(Property property) {
   removeProperty(property.getName());
  }

  /**
   * Returns an object array containing all of the properties listed by the JPF
   * defaults and the VJP defaults. While only properties defined in either JPF
   * or VJP are in this collection, values may come from the UserDefined level.
   * 
   *  @returns all of the properties defined by this configuration, including
   *           the defaults.
   */
  public Object[] getDefaultPropertiesAsArray() {
    int size = userDefined.size() + vjpDefined.size() + jpfDefined.size();
    ArrayList<Property> properties = new ArrayList<Property>(size);

    //First add in the jpfDefaults
    for (String propertyName : jpfDefined.keySet()) {
      if (!vjpDefined.containsKey(propertyName)) //Make sure this property isn't being handled by VJP
        properties.add(getProperty(propertyName));
    }
    
    //The reason why I don't just add all of the properties from JPF defined
    //is to avoid duplicate entries on the arraylist.    
    //Now add in the VJP defaults
    for (String propertyName : vjpDefined.keySet()) {
      properties.add(getProperty(propertyName));
    }

    return properties.toArray();
  }

  /**
   * Returns all Properties that are only defined by the user and not found
   * in jpf.properties or defined by VJP
   * 
   * @return an array of Properties defined by the user. These properties are
   *         not overriding any entry eithey by JPF or VJP. These properties
   *         exist soley on their own.
   */
  public Object[] getUserDefinedPropertiesAsArray() { 
    ArrayList<Property> properties = new ArrayList<Property>(userDefined.size());
    for (String propertyName : userDefined.keySet()) {
      if (!jpfDefined.containsKey(propertyName) && !vjpDefined.containsKey(propertyName))
        properties.add(getProperty(propertyName));

    }
    return properties.toArray();
  }
  

  /**
   * The IFile associated to this configuration.
   * @return The IFile associated to this configuration.
   */
  public IFile getIFile() {
    return file;
  }
  
  /**
   * Adds a change listener to this configuration.
   * Change listeners are fired once this configuration becomes dirty.
   * 
   * @param listener adds the listener to this configuration.
   */
  public void addChangeListener(PropertyChangeListener listener) {
    listeners.add(listener);
  }
  
  /**
   * Removes this change listener from this configuration.
   * 
   * @param listener the listener to be removed from this configuration.
   */
  public void removeChangeListener(PropertyChangeListener listener){
    listeners.remove(listener);
  }
  
  private void fireListeners(){
    for(PropertyChangeListener listener : listeners)
      listener.changeOccurred();
  }
  
  /**
   * Renames a property stored in this configuration
   * @param oldValue the name of the old value
   * @param newValue the value 
   */
  public void renameProperty(String oldName, String newName) {
    if (userDefined.containsKey(oldName))
      userDefined.put(newName, userDefined.remove(oldName));      
    
  }
  
  private class ConfigLine{ 
    private String line;
    public ConfigLine(){}
    public ConfigLine(String line){this.line = line;}
    public String getLine(){return line;}
  }

  private class PropertyLine extends ConfigLine{
    private String property;
    
    public PropertyLine(String property){
      super();
      this.property = property;
    }
    
    public String getPropertyName(){return property;}
    public String getLine(){return isDefined(property) ? property+"="+getPropertyValue(property) : "";}
  }
  
}


