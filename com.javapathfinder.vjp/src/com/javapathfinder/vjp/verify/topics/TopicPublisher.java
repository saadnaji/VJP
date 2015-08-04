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
    return NAME;
  }
  
  protected void setTopics(){
	setTopicItems("console");
	setTopicItems(getName());
   // setTopics("console");
   // setTopics(getName());
  }

  public Map<String,Topic> getResults(){
    return topics;
  }

  protected void openChannel(){
    output = new StringWriter();
    out = new PrintWriter(output);
  }

  protected void closeChannel(){
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