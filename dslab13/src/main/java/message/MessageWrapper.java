package message;

public class MessageWrapper implements Request, Response{

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

  public boolean isMessage() {
    return isMessage;
  }
}
