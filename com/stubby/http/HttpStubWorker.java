package com.stubby.http;

import com.stubby.core.*;
/**
 class: HttpStubWorker
 Purpose: new thread for HTTP stub.
 Notes:
 Author: Tim Lane
 Date: 24/03/2014
 
 **/

import com.sharkysoft.printf.Printf;
import com.sharkysoft.printf.PrintfTemplate;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.BufferedOutputStream;
import java.io.*;
//import java.io.IOException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Random;
import java.util.Formatter;
import java.util.Date;
import java.util.Locale;
import java.net.URL; 
import java.sql.Timestamp;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Collection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class HttpStubWorker extends StubWorker implements Runnable{
  
  public static final String DELIMITED_TYPE = "Delimited";
  public static final String MULTIDELIMITED_TYPE = "MultiDelimited";
  public static final String EXTRACTVALUE_TYPE = "ExtractValue";
  public static final String FILE_READ_TYPE = "FileRead";
  public static final String NUMBER_TYPE = "Number";
  public static final String POSITIONAL_TYPE = "Positional";
  public static final String RANDOM_DOUBLE_TYPE = "RandomDouble";
  public static final String RANDOM_LONG_TYPE = "RandomLong";
  public static final String STRING_TYPE = "String";
  public static final String THREAD_TYPE = "Thread";
  public static final String TIMESTAMP_TYPE = "Timestamp";
  public static final String LOOKUP_TYPE = "Lookup";
  public static final String HEX_TYPE = "HEX";
  public static final String GUID_TYPE = "Guid";
  public static final String THREAD_COUNT_TYPE = "ThreadCount";
  public static final String CONTENT_LENGTH_TYPE = "ContentLength";
  public static final String RECEIVEREVENT_COUNT_TYPE = "ReceiverCount";
  public static final String DATABASE_LOOKUP_TYPE = "DatabaseLookup";
  public static final String SESSION_TYPE = "SessionId";
  
  private RandomNumberGenerator randomGenerator;
  
  private Socket clientSocket;
  private HttpProperties httpProperties;
  private CoreProperties coreProperties;
  private Logger logger;
  List<String> receiverEventCntr;
  
  public HttpStubWorker(Socket clientSocket, 
                        HttpProperties httpProperties,
                        CoreProperties coreProperties,
                        Logger logger,
                        List<String> receiverEventCntr){
    this.clientSocket = clientSocket;
    this.httpProperties = httpProperties;
    this.coreProperties = coreProperties;
    this.logger = logger;  
    this.receiverEventCntr=receiverEventCntr;
  }
  
  @Override
  public void run() {
    
    HttpInputStream inStream = null;
    String searchLine = null;
    String postsearchLine = null;
    String searchType = null;
    String searchValue = null;
    String receiverName = null;
    String baselineNameMatch = null;
    String responseMsg = null;
    boolean msgFound = false;
    Double  waitFrom;
    long longWaitTime = 0;
    Double  waitTo;
    String  waitDistribution;
    String messageName; 
    String variableValue = null;
    double waitTime;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
    String formattedDate;
    String formattedStartDate;
    Date date;
    Date startDate;
    Date endDate;
    int postLength = 0;
    String firstLine = null;
    String threadName = null;
    
    ReceiverEvent receiverEvent = new ReceiverEvent() ;
    EventMessage message;

    
    // read the input
    try
    {
      httpProperties.setActiveThreadCount();
      threadName = Printf.format("%.8d", new Object[] {Double.valueOf(Thread.currentThread().getId())});
      
      /*
       * get header details 
       */
      Vector<String> inputMsgLines = new Vector<String>();
      try {
        inStream = new HttpInputStream(clientSocket.getInputStream());
        while ((searchLine = inStream.readLine()).length() > 0){
          inputMsgLines.addElement(searchLine);
          /*
           * get the content length for put post messages
           */
          if (searchLine.contains("Content-Length:")) {
            postLength = Integer.parseInt(searchLine.substring(16,(searchLine.length())));
          }
          
        }
      } catch (Exception e) {
        date = new Date();
        formattedDate = sdf.format(date);
        logger.error("httpStubWorker: " + formattedDate + " error in getting header : " + e);
        e.printStackTrace();
      }
      
      logger.debug("httpStubWorker: inputMessage : " + inputMsgLines.toString());
      /*
       * if a POST or PUT message you'll need to read the remainder of the message
       */
      try {
        firstLine = (String)inputMsgLines.elementAt(0);
      } catch (Exception e) {
        date = new Date();
        formattedDate = sdf.format(date);
        // 2.4
        logger.error("httpStubWorker: " + formattedDate + " error in getting first line : " + e);
        e.printStackTrace();
      }  
      String postLine = null;
      if (firstLine.substring(0,4).equalsIgnoreCase("POST") || 
          firstLine.substring(0,3).equalsIgnoreCase("PUT")) {
        try {
          postLine = inStream.readLine(postLength);
        } catch (Exception e) {
          date = new Date();
          formattedDate = sdf.format(date);
          // 2.4
          logger.error("httpStubWorker: " + formattedDate + " error in getting body : " + e);
          e.printStackTrace();
        }
        inputMsgLines.addElement(postLine);
      }
      
      /* 
       * have got the INPUT message now find the matching Response
       <ReceiverEvent Name="TestPageRECEIVEREVENT" KeyType="STRING" KeyValue="test_page">
       <EventMessage BaselineMessage="TestPageEVENTMESSAGE" WaitDistribution="UNIFORM" MinWait="0.2" MaxWait="1.2"/>
       </ReceiverEvent>
       <BaselineMessage Name="TestPageEVENTMESSAGE"><![CDATA[<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"><soapenv:Body>You have sucesfully hit the GoMoney server at TIMESTAMP%TIMESTAMP%TIMESTAMP</soapenv:Body></soapenv:Envelope>]]></BaselineMessage> 
       *  
       * Loop through all RECEIVEREVENT and extract matching by KEYTYPE/STRING the EVENTMESSAGE 
       * to get the corresponding BASELINEMESSAGE
       * 
       */
      searchLine = inputMsgLines.toString();
      receiverName = getReceiverName(searchLine,coreProperties);
      
      String ip = clientSocket.getRemoteSocketAddress().toString();
      if (logger.isDebugEnabled()) {
        logger.debug(sdf.format(new Date()) 
                       + " RECV : T" + threadName + " " 
                       + receiverName + " Source IP: " 
                       + ip);
      }
      if (logger.isInfoEnabled()) {
        logger.info(sdf.format(new Date()) 
                      + " RECV : T" + threadName + " " 
                      + receiverName + " " 
                      + inputMsgLines.toString().replaceAll("[\r\n]", ""));
      }
      /*
       * Once we have the receiver name then get the correspnding message config
       * to be returned. HTTP only allows one EVENT message per receiver so no need to loop through array.
       * get the wait distribution (type, min and max) from message config
       */
      
      message=getEventMessage();
      messageName=message.getName();
      waitDistribution = message.getWaitDistribution();
      waitFrom = message.getWaitFrom();
      waitTo = message.getWaitTo(); 
      
      /*
       * once we have set the message config find the corresponding BASELINE message (response data)
       * in the baseline message array
       */
      responseMsg = getBaselineMessage(messageName,coreProperties);
      
      /*
       * now we have the response message need to replace all Variables, tagged with %varName%
       * in the response message.
       * So loop through all variables and see if they exist in the response message
       */
      
      responseMsg = processResponseMessage(searchLine, responseMsg,coreProperties,logger);
      
      /*
       * we have all the required details to write a response, so it is here that we 
       * insert the delay in responding as per EVENT message config.
       */
      
      waitTime = getRandonNumber(waitDistribution,waitFrom,waitTo);
      try {
        longWaitTime = Double.valueOf(waitTime * 1000).longValue();
        Thread.sleep(longWaitTime);
      } catch (InterruptedException e) {
        date = new Date();
        formattedDate = sdf.format(date);
        logger.error("httpStubWorker: " + formattedDate + " error in thread sleep : " + e);
        e.printStackTrace();
      }
      /*
       * write the header and body
       */
      try {
        BufferedOutputStream outStream = new BufferedOutputStream(clientSocket.getOutputStream());
        /*
         * if its a close connection then dont write any data
         */
        if (!inputMsgLines.toString().contains("Connection: close")) {
          /*
           * if the mesage holds any escape codes for storage in XML , e.g. &93;
           * that require conversion at calling system then convert them to test ]
           */
          if (message.getDecodeEscape()){
            responseMsg = StringEscapeUtils.unescapeHtml4(responseMsg);
          }
          outStream.write(responseMsg.getBytes()); // write the response message.
        }
        /*
         * close all open file handles
         */ 
        outStream.flush(); 
        outStream.close();
        inStream.close();
        clientSocket.close();          
      } catch (java.io.IOException e) {
        date = new Date();
        formattedDate = sdf.format(date);
        logger.error("httpStubWorker: " + formattedDate + " error in writing message : " + e);
        e.printStackTrace();
      }
      /*
       * write the message to a log file, dependant on levle set
       */
      if (logger.isInfoEnabled()) {
        logger.info(sdf.format(new Date()) 
                      + " SEND : T" + threadName + " " 
                      + receiverName + " " 
                      + "Wait : " + String.format("%.3f", waitTime) + " "  
                      + responseMsg.replaceAll("[\r\n]", ""));
      }
      
    } catch (Exception e) { 
      date = new Date();
      formattedDate = sdf.format(date);
      logger.error("httpStubWorker: " + formattedDate + " error writing to output stream. : " + e.toString());
      e.printStackTrace();  
    } finally {
      
      /* 
       * update recever event counter
       */
      boolean receiverEventRequired = true;
      int newRecevierCnt;
      for (int z = 0; z < receiverEventCntr.size(); z++) {
        String[] receiverParts = receiverEventCntr.get(z).split(":");
        if (receiverParts[0].equals(receiverName)){  
          receiverEventRequired = false;
          newRecevierCnt = Integer.parseInt(receiverParts[1]);
          newRecevierCnt ++;
          receiverEventCntr.set(z,receiverName+":"+newRecevierCnt);
          break; // found match so stop looking  
        }
      }
      if (receiverEventRequired) {
        newRecevierCnt=1;
        receiverEventCntr.add(receiverName+":"+newRecevierCnt);
      }
    }
  }
}