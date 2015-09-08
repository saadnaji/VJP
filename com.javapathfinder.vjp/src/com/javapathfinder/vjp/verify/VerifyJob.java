/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp.verify;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.report.Publisher;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.javapathfinder.vjp.DefaultProperties;
import com.javapathfinder.vjp.VJP;
import com.javapathfinder.vjp.config.editors.ModePropertyConfiguration;
import com.javapathfinder.vjp.verify.topics.Topic;
import com.javapathfinder.vjp.verify.topics.TopicPublisher;
import com.javapathfinder.vjp.verify.topics.TopicView;
import com.javapathfinder.vjp.verify.view.VJPView;


/**
 * This Job serves to perform the JPF verification. 
 * The Verify view is created, the publisher is grabbed and verification is
 * executed all in this job.
 * @author Sandro Badame
 */
public class VerifyJob extends Job {
  /**
   * The name of this job
   */
  private static final String JOB_NAME = "Verify...";
  
  /**
   * The property that vjp looks for to add a delay to each transition in the 
   * run.
   */
  private static final String delay_key = "vjp.rundelay";
  
  /**
   * The property that vjp reads to determine whether to display the trace view
   */
  private static final String trace_listener_key = "vjp.tracelistener";
  
  
  private static boolean isRunning = false;
  
  /**
   * @return true if there is currently a VerifyJob running
   */
  public static boolean isRunning(){
    return isRunning;
  }
  
  /**
   * Automatically creates and schedules a verification
   * @param file the configuration file used to define the user defined properties
   *             for this verification
   * @param project the project to be used to determine the VJP defined values.
   * @param step if true then verification will be done step by step
   */
  public static void verify(IFile file, IProject project, boolean step){
    VJP.logInfo(project.toString());
    verify(file, JavaCore.create(project), step);
  }
  
  /**
   * Automatically creates and schedules a verification
   * @param file the configuration file used to define the user defined properties
   *             for this verification
   * @param project the project to be used to determine the VJP defined values.
   * @param step if true then verification will be done step by step
   */
  public static void verify(IFile file, IJavaProject project, boolean step){
    VerifyJob job = new VerifyJob(file, project);
    job.schedule();
  }

  private IFile file;
  private Config config;
  private boolean step = false;
  private PrintStream out, err;

  /**
   * @param modePropertyFilePath This absolute path to the Mode Property File
   *                             to be used for this verification.
   * @param project 
   */
  public VerifyJob(IFile modePropertyFile, IJavaProject project) {
    super(JOB_NAME);
    this.file = modePropertyFile;
    this.config = createConfig(modePropertyFile.getLocation().toOSString(), project);
  }
  
  private Config createConfig(String path, IJavaProject project){
    Config config = JPF.createConfig(new String[]{path});
 Enumeration<String> num = (Enumeration<String>) config.propertyNames();
    
//    while(num.hasMoreElements()) {
//    	String key = num.nextElement();
//    	System.out.println("key = " +key +", value = "+ config.getProperty(key));
//    }

    InputStream in = JPF.class.getResourceAsStream("default.properties");
   try {
		config.load(in);
		
	} catch (IOException ioe) {
	VJP.logError("Could not load InputStream.", ioe);
	} finally {
	if(in != null)
			try {
			in.close();
			} catch (IOException ioe) {
				VJP.logError("Could not close InputStream.", ioe);
			}
	}
   

//    System.out.println("the path is"+ path.toString());
//    System.out.println("the project  is "+ project.getPath().toString());
    HashMap<String, String> vjp = DefaultProperties.getDefaultProperties(project);
    HashMap<String, String> userDefined = ModePropertyConfiguration.getConfigFileProperties(file);
//    for(String key : vjp.keySet()){
//    	System.out.println("key = "+key+", value = " +  vjp.get(key));
//     }
//    
    for(String key : vjp.keySet()){
      if (!userDefined.containsKey(key))
        config.setProperty(key, vjp.get(key));
    }
    
    String publisher = userDefined.get("report.publisher");

    if (publisher == null || publisher.contains("topic")){
     // config.setProperty("jpf.report.publisher", "topic");
     // config.setProperty("jpf.report.topic.class", "com.javapathfinder.vjp.verify.topics.TopicPublisher");
    	config.setProperty("report.publisher", "topic");
    	config.setProperty("report.topic.class", TopicPublisher.class.getCanonicalName());
    	  //config.setProperty("report.publisher", "topic");
          
          
//          config.setProperty("report.console.class", "com.javapathfinder.vjp.verify.topics.TopicPublisher");
    }
	
//   System.out.println(" config contain log.level: " + config.containsKey("log.level"));

//    config.setProperty("vm.class","gov.nasa.jpf.vm.SingleProcessVM");
//    config.setProperty("vm.time.class","gov.nasa.jpf.vm.SystemTime");
//    config.setProperty("vm.fields_factory.class", "gov.nasa.jpf.vm.DefaultFieldsFactory");
    return config;
  }

  /* 
   * 
   * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  protected IStatus run(IProgressMonitor monitor) {
    monitor.beginTask("JPF Verify", 4);
    
    grabLogging();
    if (monitor.isCanceled())return cancelJob();
    monitor.worked(1);
    
    monitor.subTask("Creating JPF");
//    System.out.println(config.getProperty("vm.class"));
//    System.out.println(config.toString());
 // adding custom properties
    
    //config.setProperty("jpf.report.publisher", "topic");
   // config.setProperty("jpf.report.class", "gov.nasa.jpf.report.Reporter");
    config.setProperty("vm.bootclasspath", "${jpf.basedir}/build/env/jpf");
    config.setProperty("vm.enable_assertions", "*");
    config.setProperty("vm.por.field_boundaries.never", "java.*,javax.*,sun.*");
    
    config.setProperty("vm.por.sync_detection", "true");
//    report.console.constraint = constraint
//    		report.console.finished = result,statistics
//    		report.console.probe = statistics
//    		report.console.property_violation = error,trace,snapshot
//    		report.console.show_code = false
//    		report.console.show_method = true
  //  config.setProperty("report.console.show_steps", "true");		
    config.setProperty("report.topic.start", "jpf,sut");
    config.setProperty("report.topic.property_violation", "error,trace,snapshot");
    config.setProperty("report.topic.finished", "result,statistics");
    config.setProperty("report.topic.constraint" ,"constraint");
    config.setProperty("report.topic.probe" ,"statistics");
    config.setProperty("report.topic.show_code" ,"false");
    config.setProperty("report.topic.show_method" ,"true");
    config.setProperty("report.topic.show_steps" ,"true");
    
   // config.setProperty("cg.threads.break_all" ,"false");
    config.setProperty("search.match_depth" ,"false");
    config.setProperty("vm.bootclasspath" ,"${jpf.basedir}/build/env/jpf"); //check
    config.setProperty("vm.por.skip_finals" ,"true");
    config.setProperty("vm.finalize" ,"true");
    config.setProperty("vm.por" ,"true");
    config.setProperty("vm.max_alloc_gc" ,"-1");
    config.setProperty("vm.por.field_boundaries" ,"true");
  //  config.setProperty("report.console.finished" ,"result,statistics");
    config.setProperty("vm.visible_asserts" ,"fasle");
    config.setProperty("vm.por.fli_factory.class" ,"gov.nasa.jpf.vm.StatisticFieldLockInfoFactory");
    config.setProperty("vm.attributor.class" ,"gov.nasa.jpf.vm.DefaultAttributor");
    config.setProperty("search.heuristic.class" ,"gov.nasa.jpf.search.heuristic.BFSHeuristic");
   // config.setProperty("classpath" ,"/home/saad/runtime-EclipseApplication/lol/bin/lol");
    
    
//    config.setProperty(" vm.static_area.class" ,"false");
    
   
    
   // config.setProperty("report.console.finished", "result,statistics");

   // config.setProperty("log.handler.class", "gov.nasa.jpf.util.LogHandler");
   // config.setProperty("jpf.report.console.show_method", "true");
    //config.setProperty("jpf.report.console.finished", "result,statistics");
    //config.setProperty("jpf.report.console.constraint", "constraint");
    
 //config.setProperty("report.console.class", "com.javapathfinder.vjp.verify.topics.TopicPublisher");
    
    
    /* note check 
     * console.probe
     */
    
    
    //overwrite property
 //  config.setProperty("report.publisher", "topic");
    // this is the one
  // config.setProperty("report.console.class", "com.javapathfinder.vjp.verify.topics.TopicPublisher");
   config.setProperty("report.console.constraint", "constraint");
   config.setProperty("log.level", "severe");
   
   //end
   // config.setProperty("jpf.report.console.show_steps", "true");
    config.setProperty("report.console.property_violation", "error,trace,snapshot");
//    config.append("report.console.property_violation", "trace", ",");
    
    //remove a property
    
    //config.remove("test.report.console.finished");
    config.remove("peer_packages");
    System.out.println("I am there");
    JPF jpf = new JPF(config);
    if (monitor.isCanceled())return cancelJob();
    monitor.worked(1);
    
    if (config.getBoolean(trace_listener_key, false)){
      monitor.subTask("Adding VJP listeners");
      addListeners(jpf);
      if (monitor.isCanceled())return cancelJob();
      monitor.worked(1);
    }
    
    
    monitor.subTask("Running verification");
    try{    
        config.printEntries();
        System.out.println("Start now");
     jpf.run();  
    }catch(Exception e){
      VJP.logError("Exception while running JPF",e);
    }

    checkForTopicPublisher(jpf);
    
    monitor.subTask("Cleaning up...");
    finishJob();
    monitor.done();
    done(Status.OK_STATUS);
    return Status.OK_STATUS;
  }
  
  private IStatus cancelJob(){
    finishJob();
    done(Status.CANCEL_STATUS);
    return Status.CANCEL_STATUS;
  }
  
  private void finishJob(){
    releaseLogging();
    isRunning = false;
  }
  
  private void grabLogging(){
    MessageConsole console = new MessageConsole("Verify: "+config.getProperty("target"), null);
    ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
    MessageConsoleStream stream = console.newMessageStream();

    out = System.out;
    err = System.err;
    
    System.setOut(new PrintStream(stream));
    System.setErr(new PrintStream(stream)); 
  }
  
  private void addListeners(JPF jpf){
    setStepRun(true);
    VJPView view = VJPView.getView();
    view.setVerifyJob(this);
    
    VJPListener listener = new VJPListener(view, step);
    listener.setRunDelayMillis(config.getInt(delay_key, 0));
    jpf.addSearchListener(listener);
    jpf.addVMListener(listener);
  }
  
  private void checkForTopicPublisher(JPF jpf){
//	System.out.println("getinstance is" +  config.getStringArray("report.publisher", new String[]{"console"})[0]);
	
	  
    for (Publisher publisher : jpf.getReporter().getPublishers()){
    	

      if (publisher instanceof TopicPublisher){
    	 
        TopicView view = TopicView.getView();
        Map<String, Topic> results = ((TopicPublisher)publisher).getResults();
    	System.out.println("result size is" + results.size());

        view.showResults(results);
        return;
      }
    }
  }
  
  private void releaseLogging(){
    System.setOut(out);
    System.setErr(err);
  }
  
  
  /**
   * Sets whether the verification should just run or step.
   * @param step if true, the verification will step through each state
   */
  public void setStepRun(boolean step){
    this.step = step;
  }
  
}
