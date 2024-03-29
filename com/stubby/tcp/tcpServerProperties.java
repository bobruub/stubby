package com.stubby.tcp;

import com.stubby.core.*;

/**
class: tcpServerProperties
Purpose: holds server properties for tcp requests.
Notes:
Author: Tim Lane
Date: 24/03/2014

**/

import java.io.*;

public class tcpServerProperties {
  
  private String serverIP;
  private int ServerPort;
  private int ServerBacklog;
  
  boolean testFlag = false;
  
  public void setServerIP(String serverIP){
    this.serverIP = serverIP;
  }
  
  public String getServerIP(){
    return serverIP;
  }
  
  public void setServerPort(String ServerPort){
    this.ServerPort = Integer.parseInt(ServerPort);
  }

  public void setServerBacklog(String ServerBacklog){
    this.ServerBacklog = Integer.parseInt(ServerBacklog);
  }
  
  public int getServerPort(){
    return ServerPort;
  }
  
  public int getServerBacklog(){
    return ServerBacklog;
  }

  public boolean setTestFlag(String testFlagStr){
    if (testFlagStr.toUpperCase().matches("TRUE")){
      return this.testFlag = true;
    } else {
      return this.testFlag = false;
    }
  }

  public boolean getTestFlag(){
    return testFlag;
  }
 
}
