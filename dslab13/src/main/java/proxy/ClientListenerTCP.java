package proxy;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import message.MessageWrapper;
import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;


public class ClientListenerTCP implements Closeable, Listener {

	private Socket csocket;
	private ObjectInputStream strin;
	private ObjectOutputStream strout;

	public ClientListenerTCP() {

	}

	
	@Override
	public void close() throws IOException {
		
		strout.close();
		strin.close();
		csocket.close();
	}

  @Override
  public void listen(Object req) throws IOException {
    try {
      strout.writeObject(req);
    } catch (Exception e) {
      //proxy already closed
      System.out.println("Proxy has gone offline");
    }
  }

  @Override
  public Object receive() {
    try {
      Object o = strin.readObject();
      System.out.println("got object from type: "+o.getClass());
      if(o instanceof MessageWrapper){
        System.out.println(((MessageWrapper) o).isMessage());
        System.out.println("received");
      }
      if(o instanceof byte[] || o instanceof MessageWrapper){
        System.out.println("return object from type: "+o.getClass());
        return o;
      }
    } catch (ClassNotFoundException e)  {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }catch (IOException e)  {
      try {
        System.out.println("close TCPListener");
        close();
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
    System.out.println("encoded:");
    return null;//unreachable
  }
  
  public ObjectInputStream getInputStream(){
    return this.strin;
  }
  
  public void setSocket(Socket socket){
    this.csocket = socket;
    try {
      strin = new ObjectInputStream(csocket.getInputStream());
      strout = new ObjectOutputStream(csocket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
