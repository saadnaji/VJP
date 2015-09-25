/*******************************************************************************
 * Copyright © 2015 Saad Naji. All Rights Reserved.
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
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
 * @author Saad Naji
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
	  System.out.println("I am get name");
    return NAME;
  }
  
  protected void setTopics(){
    setTopicItems("console");
    setTopicItems(getName());
  }

  public Map<String,Topic> getResults(){
    return topics;
  }

  protected void openChannel(){
	  System.out.println("I am open channel");
    output = new StringWriter();
    out = new PrintWriter(output);
  }

  protected void closeChannel(){
	  System.out.println("Iam close channel");
    try {
      out.close();
      output.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public void publishTopicStart (String topic){
    if (topic != null)
      storeOutput();
    curTopic = topic;
  }

  public void publishEpilog(){
    storeOutput();
  }

  private void storeOutput(){
    StringBuffer buff = output.getBuffer();
    if (buff.length() > 0){
      topics.put(curTopic, new Topic( buff.toString() ) );
      buff.setLength(0); //reset the output buffer
    }
  }

}
