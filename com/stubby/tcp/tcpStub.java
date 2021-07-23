package com.stubby.tcp;

import com.stubby.core.*;

/**
 class: tcpStub
 Purpose: main method for TCP stubbing
 Notes: tcp only
 Author: Tim Lane
 Date: 24/03/2014
 Version: 
 0.1 24/03/2014 lanet - initial write
 **/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
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

import java.util.ArrayList;
import java.util.List;


public class tcpStub {
  
  private tcpProperties tcpProperties;
  private ServerSocket serverSocket;
//  private tcpBaseLineMessage tcpBaseLineMessage;
  private LogFileProperties logFileProperties;
  private static String tcpVersion = "1.1";
  
  List<String> receiverEventsCntr = new ArrayList<String>();
  
  // Create an TCP Stub for a particular TCP port
  public tcpStub(tcpProperties tcpProperties, LogFileProperties logFileProperties)
  {
    this.tcpProperties = tcpProperties;
    this.logFileProperties = logFileProperties;
  }
  
  static Logger logger = Logger.getLogger(tcpStub.class);
  public static void main(String[] args) {
    
    /*
     * get config file, need command line option
     */
    String configFileName = null;
    
    System.out.println("tcpStub: version " + tcpVersion);
    
    if (args.length > 0) {
      configFileName = args[0];
      System.out.println("tcpsStub: using config file: " + configFileName);
    } else {
      //configFileName = "C:\\dbox\\Dropbox\\java\\stubby\\xml\\tcp.xml";
      configFileName = "C:\\Users\\lanetadmin\\Documents\\java_source\\stubby\\xml\\stubby.xml";
      System.out.println("tcpsStub: using default config file: " + configFileName);
    } 
    
    try {
      /*
       * open XML config file and from xml config file read HTTP properties
       */
      XMLExtractor extractor = new XMLExtractor(new FileInputStream(new File(configFileName)));
      tcpProperties tcpProperties = new tcpProperties(extractor.getElement("TCPServer"));
      tcpProperties.setConfigFileName(configFileName);
      
      /* setup logging
       * TRACE < DEBUG < INFO < WARN < ERROR < FATAL
       * this section lets the xml config overwrite the log4j config file
       * mainly done for simplicitys sake.
       */
      LogFileProperties logFileProperties = new LogFileProperties(extractor.getElement("Header")) ;
      System.out.println("tcpsStub: log4j config file : " + logFileProperties.getLogFileName()); 
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
      System.out.println("tcpsStub: logging level set to : " + logger.getLevel().toString());
      logger.info("Non SSL version " + tcpVersion);
      
      tcpStub tcpStub = new tcpStub(tcpProperties, logFileProperties);
      tcpStub.RunIsolator();
    } catch (Exception e) {
      logger.error("error extracting XML file " + 
                   configFileName);
      e.printStackTrace();
      System.exit(1);
    }
    
  }
  
  ServerSocket getServerSocket() throws Exception {
    logger.info("Preparing a regular TCP Server Socket on server:port " + tcpProperties.getServerIP() + ":" + tcpProperties.getServerPort());
    return new ServerSocket (tcpProperties.getServerPort(),
                             tcpProperties.getServerBacklog(), 
                             InetAddress.getByName(tcpProperties.getServerIP()));
    
  }
  
  public void RunIsolator() {
    
    CoreProperties coreProperties = new CoreProperties(tcpProperties.getConfigFileName(),logger);
    /*
     * display stub information to log file
     */
    logger.info("Author: " + coreProperties.getAuthor()
                  + " Name: " + coreProperties.getName()
                  + " Description: " + coreProperties.getDescription()
                  + " Date: " + coreProperties.getDate());
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
    logger.info("setting up threadpool of size: " + tcpProperties.getThreadCount()); 
    ExecutorService executor = Executors.newFixedThreadPool(tcpProperties.getThreadCount());
    
    boolean socketLoop = true;
    boolean connectionLoop = true;
    int connectionLoopCntr = 0;
    while (socketLoop) {
      
      serverSocket = null;
      try {
        /*
         * open the socket
         */
        serverSocket = getServerSocket();
        serverSocket.setSoTimeout(5 * 1000);
        
      } catch (Exception e) {
        logger.error("Unable to listen on " + tcpProperties.getServerIP() + ":" + tcpProperties.getServerPort());
        e.printStackTrace();
        // exit on fail to bind port id.
        System.exit(1);
      }
      /*
       * listen for connections...
       */
      Socket clientConnection = null;
      while (connectionLoop){
        try {
          /*
           * accept connections a connection on a new socket
           */
          
          clientConnection = serverSocket.accept();
          clientConnection.setSoTimeout(5 * 1000);
          /*
           * Handle the connection with a separate thread                     
           */
          if (clientConnection != null) {
            Runnable tcpStubWorker = new tcpStubWorker(clientConnection, 
                                                       tcpProperties,
                                                       coreProperties,
                                                       logger,
                                                       receiverEventsCntr);
            executor.execute(tcpStubWorker);
          }
          
        } catch (SocketTimeoutException e) {
          // System.out.println("socket timeout " + connectionLoopCntr + ".");
          // DO NOTHING - The timeout just allows the checking of the restart
          // request and will only close the socket server if a restart request
          // has been issued
        } catch (Exception e) {
          logger.error("socket exception.");
          e.printStackTrace();
        } finally {
        }
      }
      /* 
       * shutdown threads
       */
      executor.shutdown();
      while (!executor.isTerminated()) {
      }
    }
  }
}

