/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.config.editors.userdefined;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.javapathfinder.vjp.config.editors.Property;

/**
 * A dialog that allows for addition of a new Property to the table.
 * 
 * @author Sandro Badame
 */
public class AddPropertyDialog extends Dialog {

  private String key = null;
  private String value = null;
  private Boolean ok_pressed = false;

  /**
   * Creates this dialog. 
   */
  public AddPropertyDialog(Shell parent) {
    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    setText("Add Property");
  }

  /**
   * Opens the dialog and returns the property
   * @return the property entered by the user, null if canceled.
   */
  public Property open() {
    // Create the dialog window
    Shell shell = new Shell(getParent(), getStyle());
    shell.setMinimumSize(400, 200);
    shell.setText(getText());
    createContents(shell);
    shell.pack();
    shell.open();
    Display display = getParent().getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    return ok_pressed ? new Property(key, value) : null;
  }

  private void createContents(final Shell shell) {
    GridLayout layout = new GridLayout();
    GridData gridData;
    layout.numColumns = 2;
    shell.setLayout(layout);
    layout.makeColumnsEqualWidth = false;

    Label label = new Label(shell, SWT.LEFT);
    label.setText("Enter the property name and value");
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    label.setLayoutData(gridData);

    Label keylabel = new Label(shell, SWT.LEFT);
    keylabel.setText("Name:");
    final Text keyText = new Text(shell, SWT.SINGLE | SWT.BORDER);
    gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    keyText.setLayoutData(gridData);

    Label valuelabel = new Label(shell, SWT.LEFT);
    valuelabel.setText("Value:");
    final Text valueText = new Text(shell, SWT.SINGLE | SWT.BORDER);
    valueText.setLayoutData(gridData);

    Button addButton = new Button(shell, SWT.PUSH);
    addButton.setText("Add");
    gridData = new GridData();
    addButton.setLayoutData(gridData);
    addButton.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
      }

      public void widgetSelected(SelectionEvent e) {
        key = keyText.getText();
        value = valueText.getText();
        ok_pressed = true;
        shell.close();
      }
    });
    shell.setDefaultButton(addButton);

    Button cancelButton = new Button(shell, SWT.PUSH);
    cancelButton.setText("Cancel");
    gridData = new GridData();
    cancelButton.setLayoutData(gridData);
    cancelButton.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
      }

      public void widgetSelected(SelectionEvent e) {
        shell.close();
      }

    });

  }
}
