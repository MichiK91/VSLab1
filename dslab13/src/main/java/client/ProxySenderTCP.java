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
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// get response
		Response res = null;
		try {
			res = (Response) strin.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			//proxy already closed
			//e.printStackTrace();
		}
		return res;
	}

	@Override
	public void close() throws IOException {
		strin.close();
		strout.close();
		socket.close();
	}

}
