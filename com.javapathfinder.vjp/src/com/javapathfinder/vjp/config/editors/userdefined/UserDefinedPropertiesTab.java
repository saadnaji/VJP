/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors.userdefined;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;

import com.javapathfinder.vjp.config.editors.ModePropertyConfiguration;
import com.javapathfinder.vjp.config.editors.Property;

/**
 * A Tab that displays all of the user defined properties
 * @author Sandro Badame
 * @author Saad Naji
 *
 */
public class UserDefinedPropertiesTab extends Composite{

  private Button add;
  private Button remove;
  private ModePropertyConfiguration properties;
  private UserDefinedPropertiesViewer viewer;

  /**
   * Constructs this tab.
   * @param parent the parent composite for this tab
   * @param properties the modepropertyconfiguration being modified
   */
  public UserDefinedPropertiesTab(Composite parent, ModePropertyConfiguration properties) {
    super(parent, SWT.NULL);
    this.properties = properties;
    createControl(this);
  }

  private void createControl(Composite parent) {
    setLayout(new FormLayout());

    Label label = new Label(parent, SWT.NULL);
    Composite table = createTable(parent, SWT.NULL);
    Composite buttons = createButtons(parent, SWT.NULL);
    
    label.setText("User defined JPF Properties");
    FormData layoutData = new FormData();
    layoutData.top = new FormAttachment(0, 5);
    layoutData.left = new FormAttachment(0, 5);
    label.setLayoutData(layoutData);
    
    int tableHeight = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).y -10;
       
    layoutData = new FormData();
    layoutData.top = new FormAttachment(label, tableHeight);
    layoutData.left = new FormAttachment(0, 5);
    layoutData.right = new FormAttachment(buttons, -5);
    layoutData.bottom = new FormAttachment(100, -10);
    table.setLayoutData(layoutData);

    layoutData = new FormData();
    layoutData.top = new FormAttachment(label, tableHeight);
    layoutData.right = new FormAttachment(100, -5);
    buttons.setLayoutData(layoutData);

  }

  private Composite createTable(Composite parent, int style) {
    Composite main = new Composite(parent, style);
    main.setLayout(new FillLayout());

    viewer = new UserDefinedPropertiesViewer(main, properties);
    viewer.setInput(properties);
    
    return main;
  }

  private Composite createButtons(Composite parent, int style) {
    Composite main = new Composite(parent, style);
    main.setLayout(new FillLayout(SWT.VERTICAL));

    add = new Button(main, SWT.PUSH);
    add.setText("Add Property");
    add.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        AddPropertyDialog addDialog = new AddPropertyDialog(getShell());
        Property property = addDialog.open();
        if (property == null)
          return;
        if (properties.isDefaultProperty(property.getName())){
          new MessageDialog(getParent().getShell(),
                            "Property is already defined.",
                            null,
                            "The property '"+property.getName()+"' is"+
                            " already defined in the default properties."+
                            " Please change its value there.",
                            MessageDialog.WARNING,
                            new String[]{"OK"},
                            0).open();
          return;
        }
        properties.setProperty(property);
        viewer.repackColumns();
        viewer.refresh();
      }
    });

    remove = new Button(main, SWT.PUSH);
    remove.setText("Remove Property");
    remove.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        for (TableItem item : viewer.getTable().getSelection()) {
          Property  p = ((Property) item.getData());
          properties.removeProperty(p);
        }
        viewer.refresh();
      }
    });

    return main;
  }
  
  /**
   * The name to be displayed on the User Defined Properties tab.
   * @return the name displayed
   */
  public String getTabName() {
    return "Custom Properties";
  }

  /**
   * Refreshes the contents of this tab.
   */
  public void refresh() {
    viewer.refresh();
    layout();
  }
}
