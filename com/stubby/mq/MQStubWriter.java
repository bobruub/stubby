package com.stubby.mq;

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
import java.io.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ibm.mq.*;

public class MQStubWriter extends StubWorker implements Runnable{

  private MQSeriesProperties mqSeriesProperties;
  private BaseLineMessage baseLineMessage;
  private String threadName = null;
  
  private CoreProperties coreProperties;
  private Logger logger;
  private MQQueue outQueue;
  private long milliSeconds;
  private MQMessage mqMessage = null;
  private MQQueue inQueue;
  private StringWriter errors = new StringWriter();
  private String errorString = null;
  
  public MQStubWriter(MQMessage mqMessage,
                      MQQueue inQueue,
                      BaseLineMessage baseLineMessage, 
                      MQSeriesProperties mqSeriesProperties,
                      CoreProperties coreProperties,
                      Logger logger){
    this.mqMessage = mqMessage;
    this.inQueue = inQueue;
    this.baseLineMessage = baseLineMessage;
    this.mqSeriesProperties = mqSeriesProperties;
    this.coreProperties = coreProperties;
    this.logger = logger;  
    
  }
  
  @Override
  public void run() {
    

    String receiverName = null;

    String responseMsg = null;
    Double  waitFrom;
    long longWaitTime = 0;
    Double  waitTo;
    String  waitDistribution;
    String messageName; 
    double waitTime;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
    String formattedDate;
    String formattedStartDate;
    Date date;
    Date startDate;
    Date endDate;
    String receivedMsg = null;
    String corrIdText = null;
    String queueName = null;
    Boolean contineProcessing = true;
    String outQueueName = null;
    MQMessage outputMmsg = null;
    MQQueueManager qMgr = null;
    String messageCountText;
    byte[] messageId;
    byte[] correlationId;
    MQGetMessageOptions messageOptions = new MQGetMessageOptions();
    messageOptions.options = MQC.MQGMO_WAIT + MQC.MQGMO_CONVERT;
    messageOptions.waitInterval = MQC.MQWI_UNLIMITED;
    
    ReceiverEvent receiverEvent = new ReceiverEvent() ;
    EventMessage message;
    String eventMessageName;
    boolean dbEventFound=false;
    BigInteger messageCount = new BigInteger("0");

    // read the input
    try
    {
      threadName = Printf.format("%.8d", new Object[] {Double.valueOf(Thread.currentThread().getId())});
      contineProcessing = true;
      try {
        /*
         * copnnect the output (from thr stub) queue
         */ 
        qMgr = new MQQueueManager(mqSeriesProperties.getQManager());
        messageOptions.options = MQC.MQGMO_WAIT + MQC.MQGMO_CONVERT;
        int openOptions = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT;
        outQueueName = mqSeriesProperties.getOutQueueName();      
        outQueue = qMgr.accessQueue(outQueueName, openOptions);
        
      }catch (Exception mqe) {
        mqe.printStackTrace(new PrintWriter(errors));
        errorString = errors.toString();
        logger.error(sdf.format(new Date()) 
                       + "MQStubWriter: Error opening queue :  T" 
                       + threadName + ". - " 
                       + errorString + " - "
                       + mqe);
        mqe.printStackTrace();
      }
      /*
       * process the message
       */
      byte[] msgBytes = new byte[mqMessage.getMessageLength()];
      mqMessage.readFully(msgBytes);
      messageId = mqMessage.messageId;
      correlationId = mqMessage.correlationId;
      receivedMsg = new String(msgBytes);
      receiverName = getReceiverName(receivedMsg,coreProperties);
      
      /*
       * if the message is not found assume no responses and processing ceases
       */ 
      if (receiverName.equals("Not Found")) {
        if (logger.isInfoEnabled()){
        logger.info(sdf.format(new Date()) 
                      + " " + milliSeconds
                      + " SINK: T" + threadName 
                      + " CORR: " + corrIdText
                      + " " +  receivedMsg.replaceAll("[\r\n]", ""));
      }
        contineProcessing = false; 
      }
      
      if (logger.isInfoEnabled()){
        corrIdText = new String(correlationId);
        milliSeconds = System.currentTimeMillis();
        logger.info(sdf.format(new Date()) 
                      + " " + milliSeconds
                      + " RECV: T" + threadName 
                      + " CORR: " + corrIdText
                      + " " +  receivedMsg.replaceAll("[\r\n]", ""));
      }
      if (contineProcessing){
        /*
         * process the message, building the response message here
         */ 
        message=getEventMessage();
        eventMessageName=message.getName();        
        waitDistribution = message.getWaitDistribution();
        waitFrom = message.getWaitFrom();
        waitTo = message.getWaitTo(); 
        responseMsg = getBaselineMessage(eventMessageName, coreProperties);
        responseMsg = processResponseMessage(receivedMsg, responseMsg, coreProperties,logger);
        // byte[] response = PureHex.write(responseMsg, "Cp101");
        waitTime = getRandonNumber(waitDistribution,waitFrom,waitTo);
        
        /*
         * pause the response simulating downstream delay
         */ 
        try {
          longWaitTime = Double.valueOf(waitTime * 1000).longValue();
          Thread.sleep(longWaitTime);
        } catch (InterruptedException e) {
          logger.error(sdf.format(new Date()) + " MQStubWriter. error in thread sleep : " + e);
          e.printStackTrace();
        }
        /*
         * write response message to MQ format
         */         

        try {
          outputMmsg = new MQMessage();
          outputMmsg.writeString(responseMsg);
          outputMmsg.correlationId = correlationId;
        }catch (Exception mqe) {
          mqe.printStackTrace(new PrintWriter(errors));
          errorString = errors.toString();
          logger.error(sdf.format(new Date())
                         + " MQStubWriter. Error processing message :  T" 
                         + threadName + ". - " 
                         + errorString + " - "
                         + mqe);
          mqe.printStackTrace();
        }
        /*
         * put the message to the oputput queue
         */ 
        try {  
          MQPutMessageOptions pmo = new MQPutMessageOptions();
          outQueue.put(outputMmsg, pmo);       
          if (logger.isInfoEnabled()) {
            milliSeconds = System.currentTimeMillis();
            logger.info(sdf.format(new Date()) 
                          + " " + milliSeconds
                          + " SEND: T" + threadName 
                          + " CORR: " + corrIdText
                          + " Wait: " + String.format("%.3f", waitTime) + " "  
                          + responseMsg.replaceAll("[\r\n]", ""));
          }
        }catch (MQException mqe) {
          mqe.printStackTrace(new PrintWriter(errors));
          errorString = errors.toString();
          logger.error(sdf.format(new Date()) 
                         + " MQStubWriter. Error writing to queue :  T" 
                         + threadName + ". - " 
                         + errorString + " - "
                         + mqe);
          mqe.printStackTrace();
        }
      }
    } catch (Exception e) { 
      e.printStackTrace(new PrintWriter(errors));
      errorString = errors.toString();
      logger.error(sdf.format(new Date()) 
                     + " MQStubWriter. Error writing to queue :  T" 
                     + threadName + ". - " 
                     + errorString + " - "
                     + e);
      e.printStackTrace();
    }finally {
      
      try {
        outQueue.close();
        qMgr.disconnect();
      } catch (MQException mqe) {
        mqe.printStackTrace(new PrintWriter(errors));
        errorString = errors.toString();
        logger.error("MQStubWriter. Error closing MQ :  T" 
                       + threadName + ". - " 
                       + errorString + " - "
                       + mqe);
        mqe.printStackTrace();
      }
    }  
  }
}