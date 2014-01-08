package server;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import message.HMACRequest;
import message.HMACResponse;
import message.Request;
import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.InfoResponse;
import message.response.ListResponse;
import message.response.MessageResponse;
import message.response.VersionResponse;
import model.HMACHandler;
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

				int counter = 3;

				do{
					// recieve object
					strin = new ObjectInputStream(csocket.getInputStream());
					Object o = strin.readObject();
					Request req = null;

					if(o instanceof HMACRequest){
						HMACRequest hmacreq = (HMACRequest) o;
						HMACHandler handler = new HMACHandler(config);
						boolean eq = handler.executeRequest(hmacreq);

						if(eq){
							Response res = sendRequestToFileServer(hmacreq.getReq());

							HMACResponse hmacres = new HMACResponse(Base64.encode(handler.generateHMAC(res)), res);

							strout = new ObjectOutputStream(csocket.getOutputStream());
							strout.writeObject(hmacres);
							break;

						} else {
							if(counter != 0){
								MessageResponse res = new MessageResponse("HMAC failed");

								HMACResponse hmacres = new HMACResponse(Base64.encode(handler.generateHMAC(res)), res);

								strout = new ObjectOutputStream(csocket.getOutputStream());
								strout.writeObject(hmacres);
							} else{
								MessageResponse res = new MessageResponse("HMAC aborted");

								HMACResponse hmacres = new HMACResponse(Base64.encode(handler.generateHMAC(res)), res);
								strout = new ObjectOutputStream(csocket.getOutputStream());
								strout.writeObject(hmacres);
								break;
							}
							counter--;
						}
					} else if(o instanceof Request){
						Request reqdown = (Request) o;
						Response res = sendRequestToFileServer(reqdown);

						strout = new ObjectOutputStream(csocket.getOutputStream());
						strout.writeObject(res);
						break;
					}

				}while(true);


				strout.close();
				strin.close();
				csocket.close();
				ssocket.close();


			} catch (IOException e) {
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
