package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import message.Request;
import message.Response;


public class ServerSenderTCP implements Closeable {
	private Socket socket;
	private ObjectOutputStream strout;
	private ObjectInputStream strin;
	private InetAddress addr;
	private int port;
	
	public ServerSenderTCP(InetAddress addr,int port){
		this.addr = addr;
		this.port = port;
		
	}
	
	private boolean connect(){
		try {
			socket = new Socket(addr, port);
		} catch (IOException e) {
			System.out.println("Cannot connect! Server has gone offline in meantime.");
			return false;
		}
		return true;
	}
	
	public Response send(Request req) throws IOException{
		if(!connect()){
			return null;
		}
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
		return res;
	}

	@Override
	public void close() throws IOException {
		if(strin != null)
			strin.close();
		if(strout != null)
			strout.close();
		if(socket != null)
			socket.close();
	}
	
}
