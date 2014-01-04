package proxy;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import util.Config;

import message.Response;
import message.response.MessageResponse;
import model.Subscriber;
import cli.Command;
import client.ClientRMI;
import client.IClientRMI;

public class ProxyRMI implements IProxyRMI{

	private Config config;
	private ProxyCli proxy;
	private Registry registry;

	public ProxyRMI(ProxyCli proxy){
		this.config = new Config("mc");
		this.proxy = proxy;


		try {
			IProxyRMI stub = (IProxyRMI) UnicastRemoteObject.exportObject(this, 0);

			registry = LocateRegistry.createRegistry(getProxyRMIPort());

			registry.bind(getBindingName(), stub);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public Response readQuorum() throws RemoteException {
		return new MessageResponse("Read-Quorom is set to " + proxy.getReadQuorum());
	}

	@Override
	public Response writeQuorum() throws RemoteException {
		return new MessageResponse("Write-Quorom is set to " + proxy.getWriteQuorum());
	}

	@Override
	public Response topThreeDownloads() throws RemoteException {
		String ret = "Top Three Downloads";
		Map<String, Integer> map = new HashMap<String, Integer>(proxy.getDownloadStats());
		if(map.isEmpty()){
			return new MessageResponse("No downloads yet");
		}

		int size = map.size();
		
		if(size >= 3){
			for(int i = 0; i < 3; i++){
				ret += "\n" + (i+1) + ". " + findMax(map);
			}
		} else{
			for(int i = 0; i < size; i++){
				ret += "\n" + (i+1) + ". " + findMax(map);
			}
			for(int i = 0; i < 3-size; i++){
				ret += "\n" + "-";
			}
		}

		return new MessageResponse(ret);
	}

	@Override
	public Response subscribe(String filename, long numberOfDownloads, String username, IClientRMI callbackobject)
			throws RemoteException {
		proxy.addSubscriber(new Subscriber(filename, numberOfDownloads, callbackobject));
		return new MessageResponse("Succesfully subscribed for file: " + filename);
	}

	@Override
	public PublicKey getProxyPublicKey() throws RemoteException {
		return proxy.getConfigPublicKey();
	}

	@Override
	public Response setUserPublicKey(String username) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void close(){
		try {
			UnicastRemoteObject.unexportObject(this, false);
		} catch (NoSuchObjectException e) {
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

	public String findMax(Map<String, Integer> map){
		int max = -1;
		String maxname = "";
		for(String s: map.keySet()){
			if(map.get(s) > max){
				max = map.get(s);
				maxname = s;
			}
		}
		map.remove(maxname);
		 
		return maxname + " " + max;
	}



}
