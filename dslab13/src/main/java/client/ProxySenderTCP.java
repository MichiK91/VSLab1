package client;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import message.Response;
import message.Request;
import util.Config;

public class ProxySenderTCP implements Closeable {
	private Config config;
	private Socket socket;
	private ObjectOutputStream strout;
	private ObjectInputStream strin;
	
	public ProxySenderTCP(Config config){
		this.config = config;
	}
	
	private void connect(){
		try {
			socket = new Socket(InetAddress.getByName(config.getString("proxy.host")), config.getInt("proxy.tcp.port"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Response send(Request req) throws IOException{
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
		return res;
	}

	@Override
	public void close() throws IOException {
		strin.close();
		strout.close();
		socket.close();
	}

}
