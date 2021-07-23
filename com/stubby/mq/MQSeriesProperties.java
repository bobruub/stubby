package com.stubby.mq;

import com.stubby.core.*;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.ibm.mq.*;

public class MQSeriesProperties {
  
  public static final String PARAM_QMGR = "QManager";
  public static final String PARAM_HOST = "ClientHost";
  public static final String PARAM_CHANNEL = "ClientChannel";
  public static final String PARAM_PORT = "ClientPort";
  public static final String PARAM_USERNAME = "ClientUserName";
  public static final String PARAM_PASSWORD = "ClientPassword";
  public static final String PARAM_QUEUENAME = "QueueName";
  public static final String PARAM_OUTQUEUENAME = "OutQueueName";
  public static final String PARAM_THREADS = "Threads";
  public static final String PARAM_WAIT = "WaitTime";
  
  public static final String PARAM_MSG_TYPE = "MsgType";
  public static final String PARAM_FORMAT = "Format";
  public static final String PARAM_ENCODER = "Encoder";
  public static final String PARAM_CCSID = "CcsId";
  
  public static String MSG_FORMAT_HEX = "HEX";
  public static String MSG_FORMAT_EBCDIC = "EBCDIC";
  public static String MSG_FORMAT_ASCII = "ASCII";
  public static String MSG_TYPE_REQUEST = "REQUEST";
  public static String MSG_TYPE_REPLY = "REPLY";
  public static String MSG_TYPE_DATAGRAM = "DATAGRAM";
  
  private int threadCount;
  String configFileName = null;
  
  private String                      qManager;
  private Element                     coreElement;
  private String                      logFile;
  private boolean debugFlag = false;
  private boolean encFlag = false;

  private int                         waitTime = 7200;
  private String                      clientChannel;
  private String                      clientHost;
  private String                      clientUserName;
  private String                      clientPassword;
  private int                         clientPort;
  private String queueName;   
  private String outQueueName;   
  private int threads;
  private String msgType;
  private String format;
  private String encoder;
  private String ccsId;
  
  public MQSeriesProperties(Element mqElement)
  {
    
    setQManager(mqElement.getAttribute(PARAM_QMGR));
    setClientHost(mqElement.getAttribute(PARAM_HOST));
    setClientChannel(mqElement.getAttribute(PARAM_CHANNEL));
    setClientPort(mqElement.getAttribute(PARAM_PORT));
    setClientUserName(mqElement.getAttribute(PARAM_USERNAME));
    setClientPassword(mqElement.getAttribute(PARAM_PASSWORD));
    setQueueName(mqElement.getAttribute(PARAM_QUEUENAME));
    setOutQueueName(mqElement.getAttribute(PARAM_OUTQUEUENAME));
    setThreads(mqElement.getAttribute(PARAM_THREADS));
    setWaitTime(mqElement.getAttribute(PARAM_WAIT));
    
    setMsgType(mqElement.getAttribute(PARAM_MSG_TYPE));
    setFormat(mqElement.getAttribute(PARAM_FORMAT));
    setEncoder(mqElement.getAttribute(PARAM_ENCODER));
    setCcsId(mqElement.getAttribute(PARAM_CCSID));
  }
  
  public void setQueueName(String queueName){
    
    
    this.queueName = queueName;
  }
  
  public String getQueueName(){
    return queueName;
  }
  
  public void setOutQueueName(String outQueueName){
    
    
    this.outQueueName = outQueueName;
  }
  
  public String getOutQueueName(){
    return this.outQueueName;
  }
  public void setThreads(String threads){
    if (threads.length() == 0){
      threads="10";
    }
    this.threads = Integer.parseInt(threads);
    
  }
  
  public int getThreads(){
    return this.threads;
  }
  
  public void setConfigFileName(String configFileName){
    this.configFileName = configFileName;
  }
  
  public String getConfigFileName(){
    return configFileName;
  }
  
  public String getQManager() {
    return qManager;
  }
  
  public void setQManager(String qManager) {
    
    this.qManager = qManager;
  }
  
  public boolean getDebugFlag() {
    return debugFlag;
  }
  
  public void setDebugFlag(boolean debugFlag) {
    this.debugFlag = debugFlag;
  }
  
  public boolean getEncFlag() {
    return encFlag;
  }
  
  public void setEncFlag(boolean encFlag) {
    this.encFlag = encFlag;
  }
  
  public String getLogFileName() {
    return logFile;
  }
  
  public void setLogFileName(String logFile) {
    this.logFile = logFile;
  }
  
  public void setCoreElement(Element coreElement)
  {
    this.coreElement = coreElement;
  }
  
  public Element getCoreElement()
  {
    return coreElement;
  }
  
  public int getWaitTime() {
    return waitTime;
  }
  
  public void setWaitTime(String waitTime) {
    if ((waitTime != null) && (waitTime.length() > 0))
      this.waitTime = Integer.parseInt(waitTime);
  }
  
  public void setWaitTime(int waitTime) {
    this.waitTime = waitTime;
  }
  
  public String getClientChannel() {
    return clientChannel;
  }
  
  public void setClientChannel(String clientChannel) {
    if ((clientChannel != null) && (clientChannel.length() > 0))
      this.clientChannel = clientChannel;
    if (clientChannel.length() == 0)
      this.clientChannel = null;
    
    
  }
  
  public String getClientUserName() {
    return clientUserName;
  }
  
  public void setClientUserName(String clientUserName) {
    if ((clientUserName != null) && (clientUserName.length() > 0))
      this.clientUserName = clientUserName;
    if (clientUserName.length() == 0)
      this.clientUserName = null;
    
  }
  
  public String getClientPassword() {
    return clientPassword;
  }
  
  public void setClientPassword(String clientPassword) {
    if ((clientPassword != null) && (clientPassword.length() > 0))
      this.clientPassword = clientPassword;
  }
  
  public String getClientHost() {
    return clientHost;
  }
  
  public void setClientHost(String clientHost) {
    if ((clientHost != null) && (clientHost.length() > 0))
      this.clientHost = clientHost;
  }
  
  public int getClientPort() {
    return clientPort;
  }
  
  public void setClientPort(String clientPort) {
    if ((clientPort != null) && (clientPort.length() > 0)){
      this.clientPort = Integer.parseInt(clientPort);
    } else {
      this.clientPort = 1414;
    }
    
  }
  
  
  
  public void setMsgType(String msgType) {
    if (msgType.length() > 0)
      this.msgType = msgType;
    else
      this.msgType = "MQMT_DATAGRAM";
  }  
  
  public String getMsgType() {
    return this.msgType;
  }
  
  public void setFormat(String format) {
    if (format.length() > 0)
      this.format = format;
    else
      this.format = "MQFMT_STRING";
  }    
  
  public String getFormat() {
    return this.format;
  }
  
  
  public void setEncoder(String encoder) {
    if (encoder.length() > 0)
      this.encoder = encoder;
    else
      this.encoder = "0";
  }    
  
  public Integer getEncoder() {
    return Integer.parseInt(this.encoder);
  }
  
  public void setCcsId(String ccsId) {
    if (ccsId.length() > 0)
      this.ccsId = ccsId;
    else 
      this.ccsId="0";
    
  }    
  
  public Integer getCcsId() {
    return Integer.parseInt(this.ccsId);
  }
  
  
  public boolean validate()
  {
    return true;
  }
  
}
