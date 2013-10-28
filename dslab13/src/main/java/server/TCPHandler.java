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

public class TCPHandler implements Runnable, Closeable{
	//TODO: Filenamen zurueckgeben
	//TODO: Downloads durchfuehren
	private Config config;
	private Socket csocket;
	private ObjectInputStream strin;
	private ObjectOutputStream strout;
	private FileServerImpl fileserver;
	private ServerSocket ssocket;

	private boolean run;

	public TCPHandler(Config config) throws SocketException {
		this.config = config;		
		this.run = false;
		this.fileserver = new FileServerImpl(config);
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
				Response res = sendRequestToFileServer(req);

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

	private Response sendRequestToFileServer(Request req) throws IOException {
		if(req.getClass().equals(ListRequest.class)){
			ListResponse lres = (ListResponse) fileserver.list();
			return lres;
		} else if(req.getClass().equals(InfoRequest.class)){
			InfoResponse ires = (InfoResponse) fileserver.info((InfoRequest) req);
			return ires;
		} else if(req.getClass().equals(VersionRequest.class)){
			VersionResponse vres = (VersionResponse) fileserver.version((VersionRequest) req);
			return vres;
		} else if(req.getClass().equals(DownloadFileRequest.class)){
			Response dres = fileserver.download((DownloadFileRequest)req);
			return dres;
		}
		
		
		/*if (req.getClass().equals(LoginRequest.class)) {
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
		}*/
		return null;
	}

	@Override
	public void close() throws IOException {
		run = false;
		fileserver.close();
		strout.close();
		strin.close();
		csocket.close();
		ssocket.close();
	}
}
