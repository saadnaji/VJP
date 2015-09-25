/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/

package com.javapathfinder.vjp.verify.topics;

import java.util.Map;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.javapathfinder.vjp.VJP;
import com.javapathfinder.vjp.verify.topics.link.LinkParser;

public class TopicView extends ViewPart {
  
  private static TopicView view = null;
  protected static final String id = "com.javapathfinder.vjp.verify.topics.view";
  private static final int[] SASH_WEIGHTS = new int[]{1,4};
  
  private List topicsList;
  private TextViewer topicDisplay;
  private Map<String, Topic> results;

  /**
   * Finds a currently opened VJP view or creates a new one. 
   * @return the VJPView found
   */
  public static TopicView getView() {
    if (view == null || view.topicsList.isDisposed() || view.topicDisplay.getTextWidget().isDisposed())
      if (Thread.currentThread() != Display.getDefault().getThread() )
        Display.getDefault().syncExec(new Runnable(){
          public void run(){
            initView();
          }
        });
      else
        initView();
    return view;
  }
  
  private static void initView(){
    try {
      view = (TopicView) PlatformUI.getWorkbench().getWorkbenchWindows()[0].getPages()[0].showView(id);
      view.SWTshowResults(null);
    } catch (PartInitException e) {
      VJP.logError(e.getMessage(),e);
    }
  }

  public void createPartControl(Composite parent) {
    SashForm s = new SashForm(parent, SWT.HORIZONTAL);
    s.setLayoutData(new GridData(GridData.FILL_BOTH));
    topicsList = new List(s, SWT.BORDER);
    
    Menu popup = new Menu(topicsList);
    MenuItem save = new MenuItem(popup, SWT.PUSH);
    save.setText("Save Results...");
    topicsList.setMenu(popup);
    
    save.addSelectionListener(new SaveReport(this));
    
    topicsList.addSelectionListener(new SelectionListener(){
      public void widgetSelected(SelectionEvent e){
        SWTtopicSelected();
      }
     
      public void widgetDefaultSelected(SelectionEvent e){
       widgetSelected(e); 
      }
    });
    
    topicDisplay = new TextViewer(s, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    topicDisplay.getTextWidget().setFont(new Font(parent.getDisplay(), "Courier", 10, SWT.NONE));
    topicDisplay.setEditable(false);
    topicDisplay.setHyperlinkPresenter(new DefaultHyperlinkPresenter(Display.getDefault().getSystemColor(SWT.COLOR_BLUE)));
    s.setWeights(SASH_WEIGHTS);
  }
  
  public void showResults(final Map<String, Topic> results){
    if (Thread.currentThread() == Display.getDefault().getThread())
      SWTshowResults(results);
    else
      Display.getDefault().asyncExec(new Runnable(){
        public void run(){
          SWTshowResults(results);
        }
      });
  }
  
  /*
   * This is not thread safe. Call from SWT thread only.
   */
  private void SWTshowResults(Map<String, Topic> results){
    setResults(results);
    topicDisplay.setDocument(new Document());
    topicsList.removeAll();
    if (results != null){
      for(String topic : results.keySet())
        if (topic != null)
          topicsList.add(topic);
      if (results.keySet().size() > 0){
        topicsList.setSelection(0);
        SWTtopicSelected();
      }
    }
  }
  
  private void setResults(Map<String, Topic> results){
    this.results = results;
  }

  
  public void setFocus() {}
  
  public Map<String, Topic> getResults(){
    return results;
  }
  
  private void SWTtopicSelected(){
    String[] selection = topicsList.getSelection();
    if (selection.length > 0){
      String content = results.get(topicsList.getSelection()[0]).getContent();
      Document document = new Document(content);
      LinkParser parser = new LinkParser();
      parser.parseText(content);
      topicDisplay.setDocument(document);
      topicDisplay.changeTextPresentation(parser.getTextPresentation(), true);
      topicDisplay.setHyperlinkDetectors(new IHyperlinkDetector[]{parser}, SWT.NULL);
    }
  }
  
}
