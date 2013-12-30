package proxy;

import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import cli.Command;

public class ProxyRMI implements IProxyRMI{

	private Config config;
	private ProxyCli proxy;

	public ProxyRMI(ProxyCli proxy){
		this.config = new Config("mc");
		this.proxy = proxy;


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
		return new MessageResponse("Read-Quorom is set to " + proxy.getReadQuroum());
	}

	@Override
	public Response writeQuorum() throws RemoteException {
		return new MessageResponse("Write-Quorom is set to " + proxy.getWriteQuorum());
	}

	@Override
	public Response topThreeDownloads() throws RemoteException {
		String ret = "Top Three Downloads";
		Map<String, Integer> map = proxy.getDownloadStats();
		if(map.isEmpty()){
			return new MessageResponse("No downloads yet");
		}

		int size = map.size();
		
		if(size >= 3){
			for(int i = 0; i < 3; i++){
				ret += i + " " + findMax(map);
			}
		} else{
			for(int i = 0; i < size; i++){
				ret += i + " " + findMax(map);
			}
			for(int i = 0; i < 3-size; i++){
				ret += "\n" + "-";
			}
		}

		return new MessageResponse(ret);
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

	public void close(){
		try {
			UnicastRemoteObject.unexportObject(this, true);
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
		 
		return "\n" + maxname + " " + max;
	}



}
