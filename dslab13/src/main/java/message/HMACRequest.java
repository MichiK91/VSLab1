package message;

import java.util.Arrays;

public class HMACRequest implements Request {

	private byte[] hmac;
	private Request req;

	public HMACRequest(byte[] hmac, Request req) {
		this.hmac = hmac;
		this.req = req;
	}

	public byte[] getHmac() {
		return hmac;
	}

	public void setHmac(byte[] hmac) {
		this.hmac = hmac;
	}

	public Request getReq() {
		return req;
	}

	public void setReq(Request req) {
		this.req = req;
	}

	@Override
	public String toString() {
		return "HMACRequest [hmac=" + Arrays.toString(hmac) + ", req=" + req + "]";
	}

}
