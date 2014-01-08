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

				boolean success = false;
				int counter = 3;
				
				do{
					// recieve object
					strin = new ObjectInputStream(csocket.getInputStream());
					Object o = strin.readObject();
					Request req = null;
					if (o instanceof Request) {
						req = (Request) o;
						// execute object
						Response res = sendRequestToFileServer(req);

						// send object
						strout = new ObjectOutputStream(csocket.getOutputStream());
						strout.writeObject(res);
						break;

					} else if(o instanceof String){
						//Upload with HMAC
						String s = (String) o;

						String[] r = s.split(" ");

						String filename = r[2];
						int version = Integer.parseInt(r[3]);
						byte[] content = r[4].getBytes();

						success = hmacEquals(r);

						if(success){
							// execute object
							Response res = sendRequestToFileServer(new UploadRequest(filename, version, content));

							strout = new ObjectOutputStream(csocket.getOutputStream());
							strout.writeObject(res);

						} else {
							
							if(counter == 0){
								strout = new ObjectOutputStream(csocket.getOutputStream());
								strout.writeObject(new MessageResponse("Upload is aborted"));
								break;
							} else {
								strout = new ObjectOutputStream(csocket.getOutputStream());
								strout.writeObject(new MessageResponse("Failed"));
							}
							counter--;
						}


					}
				}
				while(success);
				
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

	private boolean hmacEquals(String[] r){

		byte[] bhash = r[0].getBytes();
		byte[] content = r[4].getBytes();

		
		byte[] keyBytes = new byte[1024];
		String pathToSecretKey = config.getString("hmac.key");
		FileInputStream fis;
		try {
			fis = new FileInputStream(pathToSecretKey);

			fis.read(keyBytes);
			fis.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] input = Hex.decode(keyBytes);
		Key key = new SecretKeySpec(input,"HmacSHA256");


		Mac hMac = null;
		try {
			hMac = Mac.getInstance("HmacSHA256");
			hMac.init(key);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} 

		hMac.update(content);
		byte[] phash = hMac.doFinal();

		boolean b = MessageDigest.isEqual(Base64.encode(phash), bhash);
		return b;
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
