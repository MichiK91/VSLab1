package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import message.MessageWrapper;


public class ClientListenerTCP implements Closeable, Listener {

	private Socket csocket;
	private ObjectInputStream strin;
	private ObjectOutputStream strout;

	public ClientListenerTCP() {

	}


	@Override
	public void close() throws IOException {
		if(csocket != null){
			strout.close();
			strin.close();
			csocket.close();
		}
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

			if(o instanceof byte[] || o instanceof MessageWrapper){
				return o;
			}
		} catch (ClassNotFoundException e)  {
			e.printStackTrace();
		}catch (IOException e)  {
			try {
				close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
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
