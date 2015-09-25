/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.verify.view;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.javapathfinder.vjp.VJP;
import com.javapathfinder.vjp.verify.VerifyJob;
import com.javapathfinder.vjp.verify.view.TraceTable.TransitionInfo;

/**
 * This class represent the VJP view.
 * The view includes:
 * <ul>
 * <li>Control Panel - controls the execution of the verification</li>
 * <li>Trace Table - displays the current thread trace</li>
 * <li>Transition View - displays information about the selected transition</li>
 * <li>Error view - displays the errors found (if any) during the verification</li>
 * </ul>
 * @author Sandro Badame
 * @author Saad Naji
 *
 */
public class VJPView extends ViewPart implements ISelectionChangedListener{
  
  private static VJPView view;
  protected static final String id = "com.javapathfinder.vjp.vjpview";
  
  /**
   * Finds a currently opened VJP view or creates a new one. 
   * @return the VJPView found
   */
  public static VJPView getView() {  
    if (Thread.currentThread() == Display.getDefault().getThread()){
      SWTgetView();
    }else{
      Display.getDefault().syncExec(new Runnable(){
        public void run(){
          SWTgetView();
        }
      });
    }
    return view;
  }
  
  private static void SWTgetView(){
    if (view == null || view.getTraceTable().getTable().isDisposed() ){
      try {
        view = (VJPView) PlatformUI.getWorkbench().getWorkbenchWindows()[0].getPages()[0].showView(id);
      } catch (PartInitException e) {
        VJP.logError(e.getMessage(),e);
      }
    }
    view.SWTreset();
  }
  
  private static final int[] left_right_sash_weights = new int[]{1,4};
  private static final int[] transition_error_sash_weights = new int[]{5,2};
  private TransitionView transitionView;
  private TraceTable traceTable;
  private ControlPanel controlPanel;
  private ErrorViewer errorViewer;
  private VerifyJob job;

  /* 
   * Create all of this UI stuff
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent) {
    SashForm s = new SashForm(parent, SWT.HORIZONTAL );
    createTraceComposite(s, SWT.NULL);
    createDetailComposite(s, SWT.NULL);
    s.setWeights(left_right_sash_weights); 
    addListeners();
  }
  
  private void createTraceComposite(Composite parent, int style){
    Composite c = new Composite(parent, SWT.BORDER);
    c.setLayout(new FormLayout());
    FormData d = new FormData();
    d.top = new FormAttachment(0, 0);
    d.left = new FormAttachment(0, 0);
    d.right = new FormAttachment(100, 0);
    
    controlPanel = new ControlPanel(c, SWT.NULL);
    controlPanel.setLayoutData(d);
    
    Composite c2 = new Composite(c, SWT.NULL);
    c2.setLayout(new FillLayout());
    traceTable = new TraceTable(c2, SWT.BORDER);
    FormData d2 = new FormData();
    d2.top = new FormAttachment(controlPanel, 5);
    d2.bottom = new FormAttachment(100, 0);
    d2.left = new FormAttachment(0, 0);
    d2.right = new FormAttachment(100, 0);
    c2.setLayoutData(d2);
    
  }
  
  private void createDetailComposite(Composite parent, int style){
    SashForm s = new SashForm(parent, style | SWT.VERTICAL );
    transitionView = new TransitionView(s, SWT.BORDER | SWT.V_SCROLL);
    errorViewer = new ErrorViewer(s, SWT.BORDER | SWT.V_SCROLL);
    s.setWeights(transition_error_sash_weights);
  }
  
  private void addListeners(){
    traceTable.addSelectionChangedListener(this);
    
    final SelectionListener stepRunStart = new SelectionListener(){

      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e) {
        if (e.getSource().equals(controlPanel.getRunButton()))
          job.setStepRun(false);
        else if (e.getSource().equals(controlPanel.getStepButton()))
          job.setStepRun(true);
        else{
          VJP.logError("Unexpected if reached in setRunStartListener");
          return;
        }
        controlPanel.getRunButton().removeSelectionListener(this);
        controlPanel.getStepButton().removeSelectionListener(this);
        job.schedule();
      }
      
    };
    
    SelectionListener reset = new SelectionListener(){
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);        
      }
      
      public void widgetSelected(SelectionEvent e) {
        if (!e.getSource().equals(controlPanel.getResetButton()))
          return;
        
        job.cancel();
        SWTreset();
        controlPanel.getRunButton().addSelectionListener(stepRunStart);
        controlPanel.getStepButton().addSelectionListener(stepRunStart);
      }
      
    };
    
    getControlPanel().getResetButton().addSelectionListener(reset);
    getControlPanel().getResetButton().setEnabled(false);
  }
  
  
  /**
   * 
   * @return the trace table for this view
   */
  public TraceTable getTraceTable(){
    return traceTable;
  }
  
  /**
   * @return the control panel for this view
   */
  public ControlPanel getControlPanel(){
    return controlPanel;
  }
  
  /**
   * @return the error viewer for this view
   */
  public ErrorViewer getErrorViewer(){
    return errorViewer;
  }

  /**
   * Fires when the ExecutionTable selection changes
   * @param event
   */
  public void selectionChanged(SelectionChangedEvent event) {
     IStructuredSelection s = (IStructuredSelection)event.getSelection();
     TransitionInfo t = (TransitionInfo) s.getFirstElement();
     if (t != null)
       transitionView.setTransitionInfo(t);
  }

  /* 
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  public void setFocus() {}
  
  /**
   * Set the job that this view is associated with
   * @param job
   */
  public void setVerifyJob(VerifyJob job){
    this.job = job;
  }
  
  
  
  /*
   * Only call from the SWT thread
   */
  private void SWTreset(){
    traceTable.clearAll();
    traceTable.refresh();
    transitionView.setTransitionInfo(null);
    errorViewer.clearAll();
  }

  /**
   * Run the Verify job
   * @param job the Job to be run
   */
  public void runVerify(VerifyJob job) {
    setFocus();
    setVerifyJob(job);
    job.schedule();
  }

  /**
   * @return the transitionView
   */
  public TransitionView getTransitionView() {
    return transitionView;
  }
  
}
