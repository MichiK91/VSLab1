package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import message.response.MessageResponse;
import cli.Command;

import proxy.IProxyRMI;
import util.Config;

public class ClientRMI implements IClientRMI{

	private Config config;
	
	public ClientRMI(){
		this.config = new Config("mc");
		
		//init
		try {
			Registry registry = LocateRegistry.getRegistry(getProxyHost(), getProxyRMIPort());
			IProxyRMI stub = (IProxyRMI) registry.lookup(getBindingName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	@Command
	public MessageResponse readQuorum() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public MessageResponse writeQuorum() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public MessageResponse topThreeDownloads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public MessageResponse subscribe(String filename, int numberOfDownloads) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public MessageResponse getProxyPublicKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public MessageResponse setUserPublicKey(String username) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getBindingName(){
		return config.getString("binding.name");
	}
	
	public String getProxyHost(){
		return config.getString("proxy.host");
	}
	
	public int getProxyRMIPort(){
		return config.getInt("proxy.rmi.port");
	}
	
	public String getKeysDir(){
		return config.getString("keys.dir");
	}
	
}
