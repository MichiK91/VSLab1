package proxy;

import java.net.InetAddress;

import model.FileServerInfo;

public class FileServerTime {
	private InetAddress address;
	private int port;
	private long usage;
	private boolean online;
	private long timestamp;
	
	public FileServerTime(InetAddress address, int port, long usage, boolean online, long timestamp){
		this.address = address;
		this.port = port;
		this.usage = usage;
		this.online = online;
		this.timestamp = timestamp;
	}
	
	public FileServerInfo getFileServerInfo(){
		return new FileServerInfo(address, port, usage, online);
	}
	
	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getUsage() {
		return usage;
	}

	public void setUsage(long usage) {
		this.usage = usage;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileServerTime other = (FileServerTime) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (online != other.online)
			return false;
		if (port != other.port)
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (usage != other.usage)
			return false;
		return true;
	}
	
	public boolean equalsImportant(InetAddress addr, int port){
		if(this.address.equals(addr) && this.port == port){
			return true;
		}
		return false;
		
	}
}
