/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.verify.view;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.Step;
import gov.nasa.jpf.vm.Transition;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.util.Left;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.javapathfinder.vjp.verify.view.TraceTable.TransitionInfo;

/**
 * Serves as a viewer into the contents of a transition
 * @author Sandro Badame
 * @author Saad Naji
 *
 */
public class TransitionView extends ScrolledComposite {
  
  private TransitionInfo info;
  private Label label;

  /**
   * Create a new TransitionView
   * 
   * @param parent the parent of this Composite
   * @param style the stype of this Composite
   */
  public TransitionView(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());
    setExpandHorizontal(true);
    setExpandVertical(true);
    label = new Label(this, SWT.NONE);
    setContent(label);
  }
  
  /**
   * The transition information that this view will display
   * @param info the information to be displayed
   */
  public void setTransitionInfo(TransitionInfo info){
    this.info = info;
    Display.getDefault().syncExec(new Runnable(){
      public void run(){
        updateLabels();
        setMinSize(label.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      }
    });
    
  }
  
  /**
   * Update the content of this TransitionView
   *
   */
  public void updateLabels(){
    if (info == null){
      label.setText("");
      return;
    }
    Transition t = info.transition;
    label.setText(
                "Transition:\t"+ info.getTransitionNumber() +
                "\nChoice Generator type:\t"+
                t.getChoiceGenerator().getClass().getCanonicalName() +
                "\nChoice Information:\t" + t.getChoiceGenerator().toString()+
                "\nChoice:\t" +
                t.getChoiceGenerator().getProcessedNumberOfChoices() +
                "/" +
                t.getChoiceGenerator().getTotalNumberOfChoices() +
                "\nChoice Generator Insruction:\t"+
                t.getChoiceGenerator().getInsn().getSourceLine()+
                "\nThread Index:\t" +
                t.getThreadIndex()+
                "\noutput:\t"+t.getOutput()+
                "\nStep count:\t"+t.getStepCount()+
                "\nSteps:\n"+getSteps()
                
    );
  }
  
  private String getSteps(){
    StringBuilder b = new StringBuilder();
    for(Step s : info.transition ){
      //Shamlessly taken from ConsolePublisher
      String line = s.getLineString();
      if (line != null) {
        b.append('\t');
        b.append(Left.format(s.getLocationString(),30));
        b.append(" : ");
        b.append(line.trim());
      }
      Instruction insn = s.getInstruction();
      MethodInfo mi = insn.getMethodInfo();
      ClassInfo mci = mi.getClassInfo();
      b.append('\t');
      if (mci != null) {
        b.append(mci.getName());
        b.append('.');
      }
      b.append(mi.getUniqueName());
      b.append('\t');
      b.append(insn);
      b.append('\n');
    }
    return b.toString();
  }

}
