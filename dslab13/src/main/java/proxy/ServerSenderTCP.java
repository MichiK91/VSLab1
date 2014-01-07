package proxy;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import util.Config;


import message.Request;
import message.Response;
import message.request.ListRequest;
import message.request.UploadRequest;
import message.response.ListResponse;
import message.response.MessageResponse;


public class ServerSenderTCP implements Closeable {
	private Socket socket;
	private ObjectOutputStream strout;
	private ObjectInputStream strin;
	private InetAddress addr;
	private int port;

	public static void main (String [] args){
		ServerSenderTCP ss = null;
		try {
			ss = new ServerSenderTCP(InetAddress.getLocalHost(), 14342);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		byte[] content = new byte[] {(byte)0x41, (byte)0x61, (byte)0x61, (byte)0x61, (byte)0x61};


		//create key. should be in proxyimpl @upload
		Config c = new Config("proxy");
		byte[] keyBytes = new byte[1024];
		String pathToSecretKey = c.getString("hmac.key");
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
		byte[] hash = hMac.doFinal();

		byte[] base64Message = Base64.encode(hash);

		String send = new String(base64Message) + " !upload " + "up.txt " + "0 " + new String(content);
		System.out.println(send);

		try {
			System.out.println(ss.send(send));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ServerSenderTCP(InetAddress addr,int port){
		this.addr = addr;
		this.port = port;

	}

	private boolean connect(){
		try {
			socket = new Socket(addr, port);
		} catch (IOException e) {
			System.out.println("Cannot connect! Server has gone offline in meantime.");
			return false;
		}
		return true;
	}

	public Response send(Object req) throws IOException{
		if(!connect()){
			return null;
		}

		Response res = null;
		do{
			//send request
			strout = new ObjectOutputStream(socket.getOutputStream());
			strout.writeObject(req); 
			//get response
			strin = new ObjectInputStream(socket.getInputStream());
			try {
				res = (Response) strin.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			if (res.getClass().equals(MessageResponse.class)) {
				String s = ((MessageResponse)res).getMessage();
				if(!s.equals("Failed")){
					break;
					
				} else {
					System.out.println(res);
				}
			}
		}while(true);

		close();
		return res;
	}

	@Override
	public void close() throws IOException {
		if(strin != null)
			strin.close();
		if(strout != null)
			strout.close();
		if(socket != null)
			socket.close();
	}

}
