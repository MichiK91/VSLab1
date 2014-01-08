package proxy;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import message.HMACRequest;
import message.HMACResponse;
import message.Request;
import message.Response;
import message.response.MessageResponse;
import model.HMACHandler;

import org.bouncycastle.util.encoders.Base64;

import util.Config;

public class ServerSenderTCP implements Closeable {
	private Socket socket;
	private ObjectOutputStream strout;
	private ObjectInputStream strin;
	private InetAddress addr;
	private int port;

	// public static void main (String [] args){
	// ServerSenderTCP ss = null;
	// try {
	// ss = new ServerSenderTCP(InetAddress.getLocalHost(), 14342);
	// } catch (UnknownHostException e) {
	// e.printStackTrace();
	// }
	//
	// byte[] content = new byte[] {(byte)0x41, (byte)0x61, (byte)0x61, (byte)0x61, (byte)0x61};
	// UploadRequest req = new UploadRequest("test.txt", 0, content);
	// ListRequest list = new ListRequest();
	//
	//
	//
	//
	// try {
	// Response res = ss.send(list);
	// System.out.println(res);
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//

	// create key. should be in proxyimpl @upload
	// byte[] base64Message = generateHMAC(content);
	//
	// String send = new String(base64Message) + " !upload " + "up.txt " + "0 " + new String(content);
	// System.out.println(send);
	//
	// try {
	// System.out.println(ss.send(send));
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public ServerSenderTCP(InetAddress addr, int port) {
		this.addr = addr;
		this.port = port;

	}

	private boolean connect() {
		try {
			socket = new Socket(addr, port);
		} catch (IOException e) {
			System.out.println("Cannot connect! Server has gone offline in meantime.");
			return false;
		}
		return true;
	}

	public Response send(Object req) throws IOException {
		if (!connect()) {
			return null;
		}

		HMACHandler handler = new HMACHandler(new Config("proxy"));

		byte[] base64Message = Base64.encode(handler.generateHMAC((Request) req));

		HMACRequest hmacreq = new HMACRequest(base64Message, (Request) req);

		HMACResponse res = null;

		do {
			// send request
			strout = new ObjectOutputStream(socket.getOutputStream());
			strout.writeObject(hmacreq);
			// get response
			int counter = 3;
			boolean br = false;

			strin = new ObjectInputStream(socket.getInputStream());
			try {
				res = (HMACResponse) strin.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			boolean eq = handler.executeResponse(res);
			if (eq) {
				break;
			} else {
				br = true;
			}

			if (br) {
				System.out.println("False HMAC from Server!");
				break;
			}
			if (res.getRes().getClass().equals(MessageResponse.class)) {
				String s = res.getRes().toString();
				if (s.equals("HMAC failed")) {
					System.out.println(s);
				} else {
					// System.out.println(s);
					break;
				}
			} else {
				break;
			}
		} while (true);

		close();
		return res.getRes();
	}

	@Override
	public void close() throws IOException {
		if (strin != null)
			strin.close();
		if (strout != null)
			strout.close();
		if (socket != null)
			socket.close();
	}

}
