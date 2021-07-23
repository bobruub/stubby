package com.stubby.mq;

import com.stubby.core.*;
/**
class: mqStub
Purpose: main method for MQ stubbing
Notes: 
Author: Tim Lane
Date: 19/09/2015
**/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class mqStub {
  
  private MQSeriesProperties mqSeriesProperties;
  private BaseLineMessage baseLineMessage;
  private LogFileProperties logFileProperties;
  private static String mqVersion = "1.1";
  
  /*
   * Create an mqStub 
   */ 
  public mqStub(MQSeriesProperties mqSeriesProperties, LogFileProperties logFileProperties)
  {
    this.mqSeriesProperties = mqSeriesProperties;
    this.logFileProperties = logFileProperties;
  }
    
  static Logger logger = Logger.getLogger(mqStub.class);
  public static void main(String[] args) {
    
    /*
     * get config file, either from command line or default for testing.
     */
    String configFileName = null;
    
    System.out.println("mqStub: version " + mqVersion);
    
    if (args.length > 0) {
      configFileName = args[0];
    } else {
      configFileName = "C:\\dbox\\Dropbox\\java\\stubby\\xml\\mq.xml";
    } 
    System.out.println("mqStub: using config file: " + configFileName);    
    try {
      /*
       * open XML config file and from xml config file read properties
       */
      XMLExtractor extractor = new XMLExtractor(new FileInputStream(new File(configFileName)));
      MQSeriesProperties mqSeriesProperties = new MQSeriesProperties(extractor.getElement("MQSeries"));
      mqSeriesProperties.setConfigFileName(configFileName);
     /* 
      * setup logging base on XML config.
      * TRACE < DEBUG < INFO < WARN < ERROR < FATAL
      */
      LogFileProperties logFileProperties = new LogFileProperties(extractor.getElement("Header")) ;
      PropertyConfigurator.configure(logFileProperties.getLogFileName());
      if (logFileProperties.getLogLevel().toUpperCase().equals("INFO")) {
        logger.setLevel(Level.INFO);
      } else if (logFileProperties.getLogLevel().toUpperCase().equals("DEBUG")) {
        logger.setLevel(Level.DEBUG);
      } else if (logFileProperties.getLogLevel().toUpperCase().equals("WARN")) {
        logger.setLevel(Level.WARN);
      } else if (logFileProperties.getLogLevel().toUpperCase().equals("ERROR")) {
        logger.setLevel(Level.ERROR);
      } else if (logFileProperties.getLogLevel().toUpperCase().equals("FATAL")) {
       logger.setLevel(Level.FATAL);
      } else if (logFileProperties.getLogLevel().toUpperCase().equals("TRACE")) {
        logger.setLevel(Level.TRACE);
      }
      System.out.println("mqStub: logging level set to : " + logger.getLevel().toString());
      logger.info("mqStub " + mqVersion);
      mqStub mqStub = new mqStub(mqSeriesProperties, logFileProperties);
      mqStub.RunIsolator();
    } catch (Exception e) {
      logger.error("mqStub. error extracting XML file " + configFileName);
      e.printStackTrace();
      System.exit(1);
    }
   
  }
    
   
  public void RunIsolator() {
    
    /*
     * get the coreproperties from the XML file
     */
    CoreProperties coreProperties = new CoreProperties(mqSeriesProperties.getConfigFileName(),logger);
    /*
     * display stub information to log file
     */
    logger.info("Author : " + coreProperties.getAuthor()
                  + " Name : " + coreProperties.getName()
                  + " Description : " + coreProperties.getDescription()
                  + " Date : " + coreProperties.getDate());
    /*
     * load the variable configurations
     */
    for (int i = 0; i < coreProperties.getVariables().size(); i++) {
      Variable variable =  (Variable) coreProperties.getVariables().get(i);
    }
    /*
     * load the baseline response message templates
     */
    for (int i = 0; i < coreProperties.getBaselineMessages().size(); i++) {
      BaseLineMessage baseLineMessage =  (BaseLineMessage) coreProperties.getBaselineMessages().get(i);
    }
    /*
     * load the receiver events and the associated messages
     */
    for (int i = 0; i < coreProperties.getReceiverEvents().size(); i++) {
      ReceiverEvent receiverEvent =  (ReceiverEvent) coreProperties.getReceiverEvents().get(i);
      int numberOfMessages = receiverEvent.getMessages().size();
      for (int c = 0; c < numberOfMessages;  c++ ) {
        EventMessage message = (EventMessage) receiverEvent.getMessages().get(c);
      }
    }
    /*
     * setup thread pool
     */
    logger.info("setting up threadpool of size : " + mqSeriesProperties.getThreads());
    ExecutorService executor = Executors.newFixedThreadPool(mqSeriesProperties.getThreads());
    /*
     * kick off the threads up to thread count limit
     */
    for (int i = 0; i < mqSeriesProperties.getThreads(); i++) {
    
      Runnable MQStubWorker = new MQStubWorker(baseLineMessage,
                                                   mqSeriesProperties,
                                                   coreProperties,
                                                   logger);
      executor.execute(MQStubWorker);
    }
  }
  
  

  
}

