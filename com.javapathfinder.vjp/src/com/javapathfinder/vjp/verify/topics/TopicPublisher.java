package com.javapathfinder.vjp.verify.topics;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Reporter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author sandro
 */
public class TopicPublisher extends ConsolePublisher{

  private static final String NAME = "topic";

  private LinkedHashMap<String, Topic> topics;
  private StringWriter output;

  private String curTopic;

  public TopicPublisher(Config config, Reporter reporter){
    super(config, reporter);
    
    topics = new LinkedHashMap<String, Topic>();
  
  }

  public String getName(){
	  System.out.println("I am getname " + NAME);
    return NAME;
  }
  
  protected void setTopics(){
	  System.out.println(" I am store output");

	setTopicItems("console");
	//super.setTopicItems(getName());
   // setTopics("console");
     setTopicItems(getName());
  }

  public Map<String,Topic> getResults(){
	  System.out.println(" I am get result");

    return topics;
  }

  protected void openChannel(){
	  System.out.println(" I am open channel");

    output = new StringWriter();
    out = new PrintWriter(output);
  }

  protected void closeChannel(){
	  System.out.println(" I am close channel");

    try {
      out.close();
      output.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  @Override
  public void publishTopicStart (String topic){
	  System.out.println("I am publish topic");
    if (topic != null)
      storeOutput();
    curTopic = topic;
   
  }

  protected void publishEpilog(){
	  System.out.println(" I am publish Epilog");

    storeOutput();
  }

  private void storeOutput(){
	  System.out.println(" I am store output");

    StringBuffer buff = output.getBuffer();
    if (buff.length() > 0){ 
      topics.put(curTopic, new Topic( buff.toString() ) );
      buff.setLength(0); //reset the output buffer
    }
  }
 

}

//public class TopicPublisher extends ConsolePublisher{
//
//	  private static final String NAME = "topic";
//
//	  private LinkedHashMap<String, Topic> topics;
//	  private StringWriter output;
//
//	  private String curTopic;
//
//	  public TopicPublisher(Config config, Reporter reporter){
//	    super(config, reporter);
//	    topics = new LinkedHashMap<String, Topic>();
//	    
//	    // <2do> temp fix to copy existing ConsolePublisher extensions - this does not catch dynamic ones!
//	    for (Publisher p : reporter.getPublishers()){
//	      if (p instanceof ConsolePublisher){
//	        for (PublisherExtension pe : p.getExtensions()){
//	          addExtension(pe);
//	        }
//	      }
//	    }
//	  }
//
//	  @Override
//	  public String getName(){
//		  System.out.println(" I am getName");
//	    return NAME;
//	  }
//
//	  @Override
//	  protected void setTopicItems(){
//		  System.out.println(" I am setTopicItems");
//		  setTopicItems("console");
//		  setTopicItems(NAME);
//	  }
//
//	  public Map<String,Topic> getResults(){
//	    return topics;
//	  }
//
//	  @Override
//	  protected void openChannel(){
//		  System.out.println(" I am openChannel");
//	      output = new StringWriter();
//	      out = new PrintWriter(output);
//	  }
//
//	  @Override
//	  protected void closeChannel(){
//		  System.out.println(" I am closeChannel");
//	    try {
//	      out.close();
//	      output.close();
//	    } catch (IOException ex) {
//	      ex.printStackTrace();
//	    }
//	  }
//
//	  @Override
//	  public void publishTopicStart (String topic){
//		  System.out.println(" I am publishTopicStart");
//	    if (topic != null){
//	      StringBuffer buff = output.getBuffer();
//	      if (buff.length() > 0){
//	    	  topics.put(curTopic, new Topic( buff.toString() ) );
//	        buff.setLength(0); //reset the output buffer
//	      }
//	    }
//	    curTopic = topic;
//	  }
//
//	  @Override
//	  public void publishEpilog(){
//		  System.out.println(" publishEpilog");
//	    publishTopicStart("");
//	  }
//	  
//	  @Override
//	  protected void publishUser() {
//		  System.out.println("I am publishUser");
//	  }
//	  
//	  @Override
//	  protected void publishTrace() {
//		  System.out.println("I am publish trace");
//	  }
//
//	}
