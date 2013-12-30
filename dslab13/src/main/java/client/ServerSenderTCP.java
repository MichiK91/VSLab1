package client;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import message.Request;
import message.Response;

public class ServerSenderTCP implements Closeable, Sender {
	private Socket socket;
	private ObjectOutputStream strout;
	private ObjectInputStream strin;
	private InetAddress addr;
	private int port;
	
	public ServerSenderTCP(InetAddress addr,int port){
		this.addr = addr;
		this.port = port;
		
	}
	
	private void connect(){
		try {
			socket = new Socket(addr, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void send(Request req) throws IOException{
		connect();
		//send request
		strout = new ObjectOutputStream(socket.getOutputStream());
		strout.writeObject(req); 
		//get response
		strin = new ObjectInputStream(socket.getInputStream());
		Response res = null;
		try {
			res = (Response) strin.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		close();
	}

	@Override
	public void close() throws IOException {
		strin.close();
		strout.close();
		socket.close();
	}

  @Override
  public Response receive() {
    // TODO Auto-generated method stub
    return null;
  }
	
}