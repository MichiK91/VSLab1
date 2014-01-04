package server;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import message.Request;
import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.VersionResponse;
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
				e.printStackTrace();
				System.out.println("FileServer shuts down. No further downloads are allowed.");
				break;
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}

	}

	private Response sendRequestToFileServer(Request req) throws IOException {
		if (req.getClass().equals(ListRequest.class)) {
			ListResponse lres = (ListResponse) fileserver.list();
			return lres;
		} else if (req.getClass().equals(InfoRequest.class)) {
			InfoResponse ires = (InfoResponse) fileserver.info((InfoRequest) req);
			return ires;
		} else if (req.getClass().equals(VersionRequest.class)) {
			VersionResponse vres = (VersionResponse) fileserver.version((VersionRequest) req);
			return vres;
		} else if (req.getClass().equals(DownloadFileRequest.class)) {
			Response dres = fileserver.download((DownloadFileRequest) req);
			return dres;
		} else if (req.getClass().equals(UploadRequest.class)) {
			Response ures = fileserver.upload((UploadRequest) req);
			return ures;
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		if (ssocket != null)
			ssocket.close();
		fileserver.close();
	}
}
