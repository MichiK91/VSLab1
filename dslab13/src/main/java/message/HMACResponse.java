package message;

import java.io.Serializable;
import java.util.Arrays;

public class HMACResponse implements Response {
	private byte[] hmac;
	private Response res;
	
	public HMACResponse(byte[] hmac, Response res){
		this.hmac = hmac;
		this.res = res;
	}

	public byte[] getHmac() {
		return hmac;
	}

	public void setHmac(byte[] hmac) {
		this.hmac = hmac;
	}

	public Response getRes() {
		return res;
	}

	public void setRes(Response res) {
		this.res = res;
	}

	@Override
	public String toString() {
		return "HMACResponse [hmac=" + Arrays.toString(hmac) + ", res=" + res
				+ "]";
	}
	
}
