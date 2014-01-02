package client;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import message.Response;
import message.response.MessageResponse;
import cli.Command;

import proxy.IProxyRMI;
import util.Config;

public class ClientRMI extends UnicastRemoteObject implements IClientRMI, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7673656587124207060L;
	private Config config;
	private ClientCli client;
	private IProxyRMI stub;
	private Registry registry;
	
	public ClientRMI(ClientCli client) throws RemoteException{
		this.config = new Config("mc");
		this.client = client;
		
		//init
		try {
			registry = LocateRegistry.getRegistry(getProxyHost(), getProxyRMIPort());
			stub = (IProxyRMI) registry.lookup(getBindingName());
		} catch (Exception e1) {
			System.err.println("No proxy available.");
			e1.printStackTrace();
		}
	}
	
	@Override
	@Command
	public Response readQuorum() throws RemoteException {
		return stub.readQuorum();
	}

	@Override
	@Command
	public Response writeQuorum() throws RemoteException{
		return stub.writeQuorum();
	}

	@Override
	@Command
	public Response topThreeDownloads() throws RemoteException{
		return stub.topThreeDownloads();
	}

	@Override
	@Command
	public Response subscribe(String filename, long numberOfDownloads) throws RemoteException{
		if(!client.isLogin())
			return new MessageResponse("You have to log in");
		try {
			if (!client.getListOfFiles().contains(filename)) 
				return new MessageResponse("File does not exist");
		} catch (IOException e) {
			// TODO keine files vorhanden?
			e.printStackTrace();
		}
		String username = client.getUsername();
		return stub.subscribe(filename, numberOfDownloads, username, this);
	}

	@Override
	@Command
	public Response getProxyPublicKey() throws RemoteException{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public Response setUserPublicKey(String username) throws RemoteException{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void notify(String filename, long numberOfDownloads) throws RemoteException{
		client.notify(filename,numberOfDownloads);
	}
	
	public void close(){
		try {
			registry.unbind(getBindingName());
			UnicastRemoteObject.unexportObject(this, false);
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
