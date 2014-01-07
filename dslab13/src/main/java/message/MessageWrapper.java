package message;

import java.io.Serializable;

public class MessageWrapper implements Serializable{

  /**
   * 
   */
  private static final long serialVersionUID = -3106762615288610826L;
  boolean isMessage;
  byte[] content;
  
  public MessageWrapper(byte[] content, boolean isMessage){
    this.content = content;
    this.isMessage = isMessage;
  }
  
  public byte[] getContent(){
    return content;
  }

  //true if serialized message-object, false if serialized string
  public boolean isMessage() {
    return isMessage;
  }
}
