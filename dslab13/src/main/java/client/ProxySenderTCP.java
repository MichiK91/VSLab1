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

	public ProxySenderTCP(Config config) throws UnknownHostException,
			IOException {
		this.config = config;
		connect();

	}

	public void connect() throws UnknownHostException, IOException {
		socket = new Socket(InetAddress.getByName(config
				.getString("proxy.host")), config.getInt("proxy.tcp.port"));

		strout = new ObjectOutputStream(socket.getOutputStream());
		strin = new ObjectInputStream(socket.getInputStream());
	}

	public Response send(Request req) {
		
		// send request
		try {
			strout.writeObject(req);
		} catch (Exception e) {
			//proxy already closed
			System.out.println("Proxy has gone offline");
			return null;
		}

		// get response
		Response res = null;
		try {
			res = (Response) strin.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			//proxy already closed
			System.out.println("Proxy has gone offline");
			return null;
		}
		return res;
	}

	@Override
	public void close() throws IOException {
		strin.close();
		try{
			strout.close();
		}catch (Exception e) {
			//Proxy closed during connection. 
			System.out.println("Proxy has gone offline");
		}
		socket.close();
	}
}
