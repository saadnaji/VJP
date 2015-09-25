/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.verify;

import gov.nasa.jpf.ListenerAdapter;
//import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.vm.Transition;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.Error;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;

import com.javapathfinder.vjp.VJP;
import com.javapathfinder.vjp.verify.view.ControlPanel;
import com.javapathfinder.vjp.verify.view.VJPView;

/**
 * This class serves as the single link between VJP and JPF. (And between
 * Eclipse and JPF for that matter). It is a ListenerAdapater that keeps
 * track of JPF. It is also the thread the stalls a verification between
 * steps.
 * @author Sandro Badame
 *
 */
public class VJPListener extends ListenerAdapter implements SelectionListener {
 
  private VJPView view;
  
  private boolean run,step;
  private boolean cancel = false;
  
  private int delay = 0;
  
  //The lock used between the SWT Thread the JPF thread 
  private Object lock = new Object();
  
  /**
   * Creates a new instance of this listener
   * @param view that view that this listener will events to.
   * @param step whether the run should be stepped through
   */
  public VJPListener(VJPView view, boolean step){
    if (step){
      this.step = false;
      this.run = false;
    }else{
      this.step = true;
      this.run = true;
    }
    this.view = view;
    
    handleView();
  }
  
  private void handleView(){
    final SelectionListener sl = this;
    Display.getDefault().syncExec(new Runnable(){
      public void run(){
        view.getControlPanel().getRunButton().addSelectionListener(sl);
        view.getControlPanel().getStepButton().addSelectionListener(sl);
        view.getControlPanel().getResetButton().addSelectionListener(sl);
        view.getControlPanel().getResetButton().setEnabled(true);
      }
    });
    
  }
  
  public void choiceGeneratorAdvanced (VM vm) {
    String cg = vm.getChoiceGenerator().getClass().getCanonicalName();
    cg.substring(cg.lastIndexOf('.')+1);
    int choices = vm.getChoiceGenerator().getTotalNumberOfChoices();
    view.getTraceTable().newChoiceSet(cg, choices);
    view.getTraceTable().choiceAdvanced(vm.getChoiceGenerator().getProcessedNumberOfChoices());
  }

  //Search Methods
  public void stateAdvanced(Search search) {
    advanceStateTable(search);
    view.getTransitionView().setTransitionInfo(view.getTraceTable().getCurrentTransitionInfo());
    pauseRun();
    if (cancel)
      search.terminate();
  }
  
  private void advanceStateTable(Search search){
	    System.out.println("state id is " );

    Transition t = search.getTransition();
    int stateId = search.getStateId();
    boolean isEndState = search.isEndState();
    boolean isVisitedState = search.isVisitedState();
    view.getTraceTable().stateAdvanced(t, stateId, isEndState, isVisitedState);
    
  }
  
  public void stateBacktracked(Search search){
    view.getTraceTable().stateBacktrack();
    pauseRun();
    if (cancel)
      search.terminate();
  }
  
  public void searchFinished(Search search){
    removeListeners();
    for (Error e : search.getErrors())
      view.getErrorViewer().addError(e);
  }
  

  
  /**
   * Blocks the thread and waits for either run or step to be clicked.
   */
  private void pauseRun(){
    if (run)
      runPause();
    else
      stepPause();
  }
  
  private void runPause(){
    synchronized(lock){
      long end_time = System.currentTimeMillis() + delay;

      try {
        while(!cancel && System.currentTimeMillis() < end_time )
          lock.wait(delay);
      } catch (InterruptedException e) {
        VJP.logError("VJP listener run couldn't wait!!",e);
      }
    }
  }
  
  private void stepPause(){
    synchronized(lock){
      try {
        while(!cancel && !step && !run)
          lock.wait();
      } catch (InterruptedException e) {
        VJP.logError("VJP listener step couldn't wait!!",e);

      }
    }
    step = false;
  }
  
  private void removeListeners(){
    final VJPListener l = this;
    Runnable r = new Runnable(){
      public void run(){
        view.getControlPanel().getRunButton().removeSelectionListener(l);
        view.getControlPanel().getStepButton().removeSelectionListener(l);
        view.getControlPanel().getResetButton().removeSelectionListener(l);
      }
    };
    if (Thread.currentThread().equals(Display.getDefault()))
      r.run();
    else
      Display.getDefault().syncExec(r);
  }

  public void widgetSelected(SelectionEvent e) {
    ControlPanel cp = view.getControlPanel();
    if (e.getSource().equals(cp.getRunButton())){
      synchronized(lock){
        run = true;
        lock.notifyAll();
      }
    }else if(e.getSource().equals(cp.getStepButton())){
      synchronized(lock){
        step = true;
        lock.notifyAll();
      }
    }else if (e.getSource().equals(cp.getResetButton())){
      synchronized(lock){
        cancel = true;
        lock.notifyAll();
      }
    }
  }
  
  public void widgetDefaultSelected(SelectionEvent e) {
    widgetSelected(e);
   }

  /** 
   * @return The view that is associated with this listener
   */
  public VJPView getView() {
    return view;
  }

  /**
   * How long to delay between each transition when runing in milliseconds
   * @param delay the delay to set
   */
  public void setRunDelayMillis(int delay) {
    this.delay = delay;
  }
}
