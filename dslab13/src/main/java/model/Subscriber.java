package model;

import java.rmi.RemoteException;

import client.ClientRMI;

public class Subscriber {
	
		private String filename;
	private long numberOfDownloads;
	private ClientRMI callbackobject;
	
	public Subscriber(String filename, long numberOfDownloads, ClientRMI callbackobject){
		this.callbackobject = callbackobject;
		this.filename = filename;
		this.numberOfDownloads = numberOfDownloads;
	}

	public void notifyClient(){
		try {
			callbackobject.notify(filename,numberOfDownloads);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getFilename() {
		return filename;
	}

	public long getNumberOfDownloads() {
		return numberOfDownloads;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Subscriber other = (Subscriber) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (numberOfDownloads != other.numberOfDownloads)
			return false;
		return true;
	}

}
