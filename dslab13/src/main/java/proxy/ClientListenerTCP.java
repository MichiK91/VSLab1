package proxy;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;


public class ClientListenerTCP implements Runnable, Closeable, Listener {

	private Socket csocket;
	private ObjectInputStream strin;
	private ObjectOutputStream strout;
	private ProxyCli proxycli;
	private ProxyImpl proxy;

	private boolean run;

	public ClientListenerTCP(Socket csocket, ProxyCli proxycli) {
		this.run = false;
		this.proxycli = proxycli;
		proxy = new ProxyImpl(this.proxycli);

		this.csocket = csocket;
		try {
			strin = new ObjectInputStream(csocket.getInputStream());
			strout = new ObjectOutputStream(csocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		run = true;
		while (run) {

			// recieve object
			try {
				Object o = strin.readObject();
				this.listen(o);
				
				
				Request req = null;
				if (o instanceof Request) {
					req = (Request) o;
					System.out.println(req);
				}
        
				
				// execute object
				Response res = sendRequestToProxy(req);

				// send object
				strout.writeObject(res);
			} catch (EOFException e) {
				// client closed connection
				System.out.println("Client closed connection.");
				break;
			} catch (IOException e) {
				// socket closed
				break;
			} catch (ClassNotFoundException e) {
				break;
			}

		}
	}

	private Response sendRequestToProxy(Request req) throws IOException {
		if (req.getClass().equals(LoginRequest.class)) {
			return proxy.login((LoginRequest) req);
		} else if (req.getClass().equals(BuyRequest.class)) {
			return proxy.buy((BuyRequest) req);
		} else if (req.getClass().equals(CreditsRequest.class)) {
			return proxy.credits();
		} else if (req.getClass().equals(ListRequest.class)) {
			return proxy.list();
		} else if (req.getClass().equals(DownloadTicketRequest.class)) {
			return proxy.download((DownloadTicketRequest) req);
		} else if (req.getClass().equals(UploadRequest.class)) {
			return proxy.upload((UploadRequest) req);
		} else if (req.getClass().equals(LogoutRequest.class)) {
			return proxy.logout();
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		proxy.close();
		run = false;
		strout.close();
		strin.close();
		csocket.close();
	}

  @Override
  public void listen(Object req) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Object receive() {
    try {
      Object o = strin.readObject();
      return o;
    } catch (ClassNotFoundException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  public ObjectInputStream getInputStream(){
    return this.strin;
  }

}
