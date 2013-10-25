package proxy;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import message.Request;
import message.Response;
import message.request.BuyRequest;
import message.request.CreditsRequest;
import message.request.DownloadFileRequest;
import message.request.DownloadTicketRequest;
import message.request.ListRequest;
import message.request.LoginRequest;
import message.request.LogoutRequest;
import message.request.UploadRequest;

import util.Config;

public class ClientListener implements Runnable, Closeable {

	private Config config;
	private ServerSocket ssocket;
	private Socket csocket;
	private ObjectInputStream strin;
	private ObjectOutputStream strout;
	private ProxyImpl proxy;

	private boolean run;

	public ClientListener(Config config, ProxyImpl proxy) throws SocketException {
		this.config = config;		
		this.run = false;
		this.proxy = proxy;
	}

	@Override
	public void run() {
		try {

			run = true;
			while (run) {

				ssocket = new ServerSocket(config.getInt("tcp.port"));
				csocket = ssocket.accept();

				// recieve object
				strin = new ObjectInputStream(csocket.getInputStream());
				Object o = strin.readObject();
				Request req = null;
				if (o instanceof Request) {
					req = (Request) o;
					System.out.println(req);
				}

				// execute object
				Response res = sendRequestToProxy(req);

				// send object
				strout = new ObjectOutputStream(csocket.getOutputStream());
				strout.writeObject(res);
				
				strout.close();
				strin.close();
				csocket.close();
				ssocket.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
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
		run = false;
		strout.close();
		strin.close();
		csocket.close();
		ssocket.close();
	}

}
