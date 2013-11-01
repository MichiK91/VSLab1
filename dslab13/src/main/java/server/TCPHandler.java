package server;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import message.Request;
import message.Response;
import message.request.*;
import message.response.*;
import util.Config;

public class TCPHandler implements Runnable, Closeable {
	private Config config;
	private Socket csocket;
	private ObjectInputStream strin;
	private ObjectOutputStream strout;
	private FileServerImpl fileserver;
	private ServerSocket ssocket;

	public TCPHandler(Config config) {
		this.config = config;
		this.fileserver = new FileServerImpl(config);
	}

	@Override
	public void run() {

		while (true) {
			try {
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
				Response res = sendRequestToFileServer(req);

				// send object
				strout = new ObjectOutputStream(csocket.getOutputStream());
				strout.writeObject(res);

				strout.close();
				strin.close();
				csocket.close();
				ssocket.close();
			} catch (IOException e) {
				break;
			} catch (ClassNotFoundException e) {
				//TODO
			}
		}

	}

	private Response sendRequestToFileServer(Request req) throws IOException {
		if (req.getClass().equals(ListRequest.class)) {
			ListResponse lres = (ListResponse) fileserver.list();
			return lres;
		} else if (req.getClass().equals(InfoRequest.class)) {
			InfoResponse ires = (InfoResponse) fileserver
					.info((InfoRequest) req);
			return ires;
		} else if (req.getClass().equals(VersionRequest.class)) {
			VersionResponse vres = (VersionResponse) fileserver
					.version((VersionRequest) req);
			return vres;
		} else if (req.getClass().equals(DownloadFileRequest.class)) {
			Response dres = fileserver.download((DownloadFileRequest) req);
			return dres;
		} else if (req.getClass().equals(UploadRequest.class)) {
			Response ures = fileserver.upload((UploadRequest) req);
			return ures;
		}

		/*
		 * if (req.getClass().equals(LoginRequest.class)) { return
		 * proxy.login((LoginRequest) req); } else if
		 * (req.getClass().equals(BuyRequest.class)) { return
		 * proxy.buy((BuyRequest) req); } else if
		 * (req.getClass().equals(CreditsRequest.class)) { return
		 * proxy.credits(); } else if (req.getClass().equals(ListRequest.class))
		 * { return proxy.list(); } else if
		 * (req.getClass().equals(DownloadTicketRequest.class)) { return
		 * proxy.download((DownloadTicketRequest) req); } else if
		 * (req.getClass().equals(UploadRequest.class)) { return
		 * proxy.upload((UploadRequest) req); } else if
		 * (req.getClass().equals(LogoutRequest.class)) { return proxy.logout();
		 * }
		 */
		return null;
	}

	@Override
	public void close() throws IOException {
		if(ssocket != null)
			ssocket.close();
		fileserver.close();
	}
}
