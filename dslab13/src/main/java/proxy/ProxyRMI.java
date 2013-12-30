package proxy;

import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import util.Config;

import message.Response;
import cli.Command;

public class ProxyRMI implements IProxyRMI{
	
	private Config config;
	private ProxyCli proxy;

	public ProxyRMI(/*ProxyCli proxy*/){
		this.config = new Config("mc");
		//this.proxy = proxy; //TODO
		
		//TODO in Proxy auslagern
		try {
			IProxyRMI stub = (IProxyRMI) UnicastRemoteObject.exportObject(this, 0);

			Registry regestry = LocateRegistry.createRegistry(getProxyRMIPort());

			regestry.bind(getBindingName(), stub);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public Response readQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response writeQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response topThreeDownloads() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response subscribe(String filename, long numberOfDownloads)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getProxyPublicKey() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response setUserPublicKey(String username) throws RemoteException {
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
