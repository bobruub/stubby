package com.stubby.mq;

import com.stubby.core.*;
/**
 class: mqStubWorker
 Purpose: new thread for mq stub.
 Notes:
 Author: Tim Lane
 Date: 24/03/2014
 
 **/
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sharkysoft.printf.Printf;
import com.sharkysoft.printf.PrintfTemplate;
import java.io.*;
import java.math.BigInteger;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Random;
import java.util.Formatter;
import java.util.Date;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.ibm.mq.*;

public class MQStubWorker extends StubWorker implements Runnable{
 
  private MQSeriesProperties mqSeriesProperties;
  private BaseLineMessage baseLineMessage;
  String threadName = null;
  
  private CoreProperties coreProperties;
  private Logger logger;
  private MQQueue outQueue;
  private long milliSeconds;
  
  public MQStubWorker(BaseLineMessage baseLineMessage, 
                      MQSeriesProperties mqSeriesProperties,
                      CoreProperties coreProperties,
                      Logger logger){
    this.baseLineMessage = baseLineMessage;
    this.mqSeriesProperties = mqSeriesProperties;
    this.coreProperties = coreProperties;
    this.logger = logger;  
  }
  
  @Override
  public void run() {
    
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
    String formattedDate;
    String formattedStartDate;
    Date date;
    Date startDate;
    Date endDate;
    
    String queueName = null;
    Boolean mqSeriesLoop = true;
    MQMessage mqMessage = null;
    String outQueueName = null;
    MQMessage outputMmsg = null;;
    MQQueueManager qMgr;
    String messageCountText;
    
    BigInteger messageCount = new BigInteger("0");
    // read the input
    try
    {
      threadName = Printf.format("%.4d", new Object[] {Double.valueOf(Thread.currentThread().getId())});
      if (logger.isDebugEnabled()){
        logger.debug("mqSeries Client Channel: " + mqSeriesProperties.getClientChannel());
        logger.debug("mqSeries Client host: " + mqSeriesProperties.getClientHost());
        logger.debug("mqSeries Client port: " + mqSeriesProperties.getClientPort());
        logger.debug("mqSeries Client user: " + mqSeriesProperties.getClientUserName());
        logger.debug("mqSeries Client password: " + mqSeriesProperties.getClientPassword());
        logger.debug("mqSeries queue manager: " + mqSeriesProperties.getQManager());
        logger.debug("mqSeries inbound queue: " + mqSeriesProperties.getQueueName());
        logger.debug("mqSeries outbound queue: " + mqSeriesProperties.getOutQueueName());
      }
      /*
       * if client connections details are set then use them 
       * otherwise assume local Server manager.
       */
      if (mqSeriesProperties.getClientChannel() != null) {
        MQEnvironment.channel = mqSeriesProperties.getClientChannel();
        MQEnvironment.hostname = mqSeriesProperties.getClientHost();
        MQEnvironment.port = mqSeriesProperties.getClientPort();
        MQEnvironment.userID = mqSeriesProperties.getClientUserName();
        MQEnvironment.password = mqSeriesProperties.getClientPassword(); 
      }
      /*
       * connect to the queue manage server and input (to stub) queue
       */
      qMgr = new MQQueueManager(mqSeriesProperties.getQManager());
      MQGetMessageOptions messageOptions = new MQGetMessageOptions();
      messageOptions.options = MQC.MQGMO_WAIT + MQC.MQGMO_CONVERT;
      int openOptions = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT;
      // if the message has an QueueName defined use that otherwise MQSERIES default.
      queueName = mqSeriesProperties.getQueueName();
      if (baseLineMessage.getOutQueueName().length() > 0){
        outQueueName = baseLineMessage.getOutQueueName();
      } else {
        outQueueName = mqSeriesProperties.getOutQueueName();
      } 
      MQQueue inQueue = qMgr.accessQueue(queueName, MQC.MQOO_INPUT_AS_Q_DEF);
      outQueue = qMgr.accessQueue(outQueueName, openOptions);
      messageOptions.waitInterval = MQC.MQWI_UNLIMITED;
      if (mqSeriesProperties.getEncoder() != 0){
        mqMessage.encoding = mqSeriesProperties.getEncoder();
      }
      if (mqSeriesProperties.getCcsId() != 0){
        mqMessage.characterSet = mqSeriesProperties.getCcsId();
      }
      
      /*
       * setup a pool of threads for processing
       */
      ExecutorService executor = Executors.newFixedThreadPool(10);
      while (mqSeriesLoop){
        /*
         * get the message off the input queue
         */
        mqMessage = new MQMessage();
        inQueue.get(mqMessage, messageOptions);
   //     messageCount = messageCount.add(BigInteger.valueOf(1));
   //     messageCountText = messageCount.toString();
        
        /*
         * process the message in a new thread
         */ 
        
        Runnable MQStubWriter = new MQStubWriter(mqMessage,
                                                 inQueue,
                                                 baseLineMessage,
                                                 mqSeriesProperties,
                                                 coreProperties,
                                                 logger);
        
        executor.execute(MQStubWriter);
      }
    } catch (Exception e){
      logger.error("mqSubWorker. mq exception " + e);
      e.printStackTrace();
    }
    
    
    
  }
}