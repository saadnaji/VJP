/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.javapathfinder.vjp.VJP;
import com.javapathfinder.vjp.config.LaunchDialog;
import com.javapathfinder.vjp.config.editors.defaultproperties.DefaultPropertiesTab;
import com.javapathfinder.vjp.config.editors.userdefined.UserDefinedPropertiesTab;

/**
 * This composite holds all of the modeproperty editors.
 * @author Sandro Badame
 */
public class ModePropertyEditorComposite extends Composite implements SelectionListener, PropertyChangeListener{
  private static final int DEFAULT_STYLE = SWT.NULL;
  
  private ModePropertyConfiguration properties;
  private Button save;
  private Button revert;
  
  private DefaultPropertiesTab defaultPropertiesTab;
  private UserDefinedPropertiesTab userDefinedPropertiesTab;
  
  /**
   * Constructs this composite to hold all of the modeproperty editors.
   * @param parent the parent for this composite
   * @param project the java project associated to the modepropertyconfiguration
   * @param properties the modepropertyconfiguration being modified.
   */
  public ModePropertyEditorComposite(Composite parent, ModePropertyConfiguration properties) {
    super(parent, DEFAULT_STYLE);
    this.properties = properties;
    setLayout(new FormLayout());
    createContents(this);
    properties.addChangeListener(this);
  }
  
  /**
   * Constructs this composite to hold all of the modeproperty editors.
   * @param parent the parent for this composite
   * @param project the java project associated to the modepropertyconfiguration
   * @param file the modepropertyfile being modified.
   */
  public ModePropertyEditorComposite(Composite parent, IJavaProject project, IFile configFile){
    this(parent,  new ModePropertyConfiguration(configFile, project));
  }

  private void createContents(Composite parent) {
    Composite fileInfo = createFileInfoUI(parent);
    Composite editorTabs = createEditorTabs(parent);
    Composite saverevert = createSaveRevertButtons(parent);
    
    FormData layoutData = new FormData();
    layoutData.top = new FormAttachment(0, 10);
    layoutData.left = new FormAttachment(0, 10);
    layoutData.right = new FormAttachment(100, -10);
    fileInfo.setLayoutData(layoutData);   
    
    layoutData = new FormData();
    layoutData.top = new FormAttachment(fileInfo, 10);
    layoutData.left = new FormAttachment(0, 10);
    layoutData.right = new FormAttachment(100, -10);
    layoutData.bottom = new FormAttachment(saverevert, -10);
    editorTabs.setLayoutData(layoutData);
    
    layoutData = new FormData();
    layoutData.left = new FormAttachment(0, 10);
    layoutData.right = new FormAttachment(100, -10);
    layoutData.bottom = new FormAttachment(100, -10);
    saverevert.setLayoutData(layoutData);

  }

  private Composite createFileInfoUI(Composite parent) {
    Group group = new Group(parent, SWT.NULL);
    group.setText("Configuration file location:");
    group.setLayout(new FormLayout());
    
    Button button = new Button(group, SWT.NULL);
    button.setText("Move/Rename");
    FormData buttonData = new FormData();

    buttonData.right = new FormAttachment(100, -5);
    button.setLayoutData(buttonData);

    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IPath path = getNewPath();
        if (path == null)
          return;
        int matches = path.matchingFirstSegments(ResourcesPlugin.getWorkspace().getRoot().getLocation());
        path = path.removeFirstSegments(matches).makeAbsolute();
        try{   
          properties.getIFile().move(path, true, null);
          properties.getIFile().refreshLocal(IFile.DEPTH_INFINITE, null);
          refreshDialog(properties.getIFile());
        }catch (CoreException e1) {
          VJP.logError("Could not move property file.", e1);
        }
      }
      
      private void refreshDialog(IFile file){
        ((LaunchDialog)(getShell().getData())).updateTree();
      }
      
      private IPath getNewPath(){
        ModePropertyFileDialog dialog = new ModePropertyFileDialog(getShell(), properties);
        IFile file = dialog.getFile();
        IProject project = dialog.getFileProject();
        if (project == null){
          new MessageDialog(getShell(),
                            "Invalid Mode Property Location",
                            null,
                            "Mode Property Files must be kept within a project",
                            MessageDialog.ERROR,
                            new String[]{"OK"},
                            0).open();
          return null;
        }
        if (file.equals(properties.getIFile()))
          return null;
        
        return file.getLocation();
      }
      
    });

    Text configPathField = new Text(group, SWT.SINGLE | SWT.LEFT);
    configPathField.setText(properties.getIFile().getProjectRelativePath().toOSString());
    configPathField.setEditable(false);
   
    FormData textData = new FormData();
    textData.left = new FormAttachment(0, 10);
    textData.right = new FormAttachment(button, -5);

    Point buttonsize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    Point textsize = configPathField.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    int diff = Math.abs(buttonsize.y - textsize.y);
    diff /= 2;
    textData.top = new FormAttachment(0, diff);

    configPathField.setLayoutData(textData);

    return group;
  }
  

  private Composite createEditorTabs(Composite parent) {
   TabFolder tabs = new TabFolder(parent, SWT.NULL);
   
   TabItem heuristicsTabItem = new TabItem(tabs, SWT.NULL);
   userDefinedPropertiesTab = new UserDefinedPropertiesTab(tabs, properties);
   heuristicsTabItem.setControl(userDefinedPropertiesTab);
   heuristicsTabItem.setText(userDefinedPropertiesTab.getTabName());
   
   TabItem propertyTabItem = new TabItem(tabs, SWT.NULL);
   defaultPropertiesTab = new DefaultPropertiesTab(tabs, properties);
   propertyTabItem.setControl(defaultPropertiesTab);
   propertyTabItem.setText(defaultPropertiesTab.getTabName());
   
   return tabs;
  }
  
  private Composite createSaveRevertButtons(Composite parent) {
    Composite top = new Composite(parent, SWT.NULL);
    top.setLayout(new FormLayout());
    
    save = new Button(top, SWT.NULL);
    save.setEnabled(false);
    save.setText("Save");
    save.addSelectionListener(this);
    save.setToolTipText("Saves the changes made to the modeproperties file.");
    
    revert = new Button(top, SWT.NULL);
    revert.setEnabled(false);
    revert.setText("Revert");
    revert.addSelectionListener(this);
    revert.setToolTipText("Reverts the properties displayed to those stored "+
                          "in the configuration file.");
    
    FormData data = new FormData();
    data.right = new FormAttachment(revert, -5);
    save.setLayoutData(data);
    
    data = new FormData();
    data.right = new FormAttachment(100, -5);
    revert.setLayoutData(data);
    
    return top;
  }

  /**
   * Executed when save or revert is clicked on.
   */
  public void widgetSelected(SelectionEvent e) {
    if (e.widget.equals(save))
      saveProperties();
    else if (e.widget.equals(revert))
      revertProperties();
    refresh();
  }
  
  public void widgetDefaultSelected(SelectionEvent e) {
    widgetSelected(e);
  }
  
  /**
   * Saves the properties being modified to the configuration file
   */
  public void saveProperties(){
    try {
      properties.save();
      setButtonsEnabled(false);
    } catch (IOException e) {
      VJP.logError("Could not save file.", e);
    } catch (CoreException e) {
      VJP.logError("Could not save file.", e);
    }
  }
  
  /**
   * Reverts this editor show the properties contained in the file.
   *
   */
  public void revertProperties(){
    try {
      properties.reloadFromFile();
      setButtonsEnabled(false);
    } catch (IOException ioe) {
      VJP.logError("IO exception when trying to reload from file.", ioe);
    } catch (CoreException e) {
      VJP.logError("Core exception when trying to reload from file.", e);
    }
  }
  
  private void setButtonsEnabled(boolean enabled){
    save.setEnabled(enabled);
    revert.setEnabled(enabled);
  }
  
  private void refresh(){
    defaultPropertiesTab.refresh();
    userDefinedPropertiesTab.refresh();
  }

  /*
   * Executed when a change to the properties being modified occurs
   * Enables the save and revert buttons.
   * (non-Javadoc)
   * @see com.javapathfinder.vjp.launch.editors.PropertyChangeListener#changeOccurred()
   */
  public void changeOccurred() {
    setButtonsEnabled(true);
  }
  
  /*
   * True if there are changes to be saved to the config file.
   */
  public boolean isDirty(){
    return save.isEnabled();
  }
  
  /**
   * returns the mode property configuration this editor is modifying.
   *@return modepropertyconfiguration
   */
  public ModePropertyConfiguration getModePropertyConfiguraton() {
    return properties;    
  }
}
