package com.stubby.core;

/**
class: EventMessage
Purpose: setup the event message.
Notes:
Author: Tim Lane
Date: 25/03/2014

**/

public class EventMessage {
  
  private BaseLineMessage template;
  private boolean DecodeEscape = false;
  private Double  waitFrom;
  private Double  waitTo;
  private String  waitDistribution;
  private String eventName;
  
  public BaseLineMessage getTemplate() {
    return template;
  }

  public void setTemplate(BaseLineMessage tmp) {
    template = tmp;
  }
  
  public void setName(String eventName){
    this.eventName = eventName;
    
  }
  
  public String getName(){
    return this.eventName;
  }
  
    
  public String getWaitDistribution() {
    return waitDistribution;
  }

  public void setWaitDistribution(String waitDistribution) {
    this.waitDistribution = waitDistribution;
  }

  public Double getWaitFrom() {
    return waitFrom;
  }

  public void setWaitFrom(Double waitFrom) {
    this.waitFrom = waitFrom;
  }
    
  public void setWaitFrom(String waitFrom) {
    this.waitFrom = Double.parseDouble(waitFrom);
  }

  public Double getWaitTo() {
    return this.waitTo;
  }

  public void setWaitTo(Double waitTo) {
    this.waitTo = waitTo;
  }

  public void setWaitTo(String waitTo) {
    this.waitTo = Double.parseDouble(waitTo);
  }
  
  public boolean getDecodeEscape() {
    return DecodeEscape;
  }
    
  public void setDecodeEscape(boolean DecodeEscape) {
    this.DecodeEscape = DecodeEscape;
  }
    
  public void setDecodeEscape(String DecodeEscape) {
    if (DecodeEscape.toUpperCase().matches("TRUE")){
            setDecodeEscape(true);
        } else{
            setDecodeEscape(false);
        }
    }
  
    
}
