package com.stubby.core;

/**
 class: BaseLineMessage
 Purpose: setup the baseline respopnse message.
 Notes:
 Author: Tim Lane
 Date: 25/03/2014
 
 **/

public class Utils {
  
  String convertedMessage;
  
  public String convertHexToString(String hexMessage){
    
    StringBuilder sb = new StringBuilder();
    StringBuilder temp = new StringBuilder();
    //49204c6f7665204a617661 split into two characters 49, 20, 4c...
    for( int i=0; i<hexMessage.length()-1; i+=2 ){
      //grab the hex in pairs
      String output = hexMessage.substring(i, (i + 2));
      //convert hex to decimal
      int decimal = Integer.parseInt(output, 16);
      //convert the decimal to character
      sb.append((char)decimal);
      temp.append(decimal);
    }
    return sb.toString();
  }
  
  public String convertStringToHex(String stringMessage){
    
    char[] chars = stringMessage.toCharArray();
    StringBuffer hex = new StringBuffer();
    for(int i = 0; i < chars.length; i++){
      hex.append(Integer.toHexString((int)chars[i]));
    }
    return hex.toString();
  }
}
