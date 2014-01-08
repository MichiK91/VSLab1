package model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import message.HMACRequest;
import message.HMACResponse;
import message.Request;
import message.Response;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import util.Config;

public class HMACHandler {
	private Config config;
	
	public HMACHandler(Config config){
		this.config = config;
	}
	
	public boolean executeRequest(HMACRequest req){
		//generate HMAC
		byte[] pt_hmac = generateHMAC(req.getReq());
		
		//equals
		boolean b = MessageDigest.isEqual(Base64.encode(pt_hmac), req.getHmac());
		return b;
	}
	
	public boolean executeResponse(HMACResponse res){
		//generate HMAC
		byte[] pt_hmac = generateHMAC(res.getRes());
		
		//equals
		boolean b = MessageDigest.isEqual(Base64.encode(pt_hmac), res.getHmac());
		return b;
	}
	
	
	public byte[] generateHMAC(Object o){
		Serializable req = null;
		if(o instanceof Request){
			req = (Request) o;
		}else if(o instanceof Response){
			req = (Response) o;
		}

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

		hMac.update(req.toString().getBytes());
		byte[] phash = hMac.doFinal();

		return phash;
	}
		

}
