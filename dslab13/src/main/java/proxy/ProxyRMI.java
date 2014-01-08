package proxy;

import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import message.Response;
import message.response.MessageResponse;
import model.Subscriber;
import util.Config;
import client.IClientRMI;

public class ProxyRMI implements IProxyRMI {

	private Config config;
	private ProxyCli proxy;
	private Registry registry;

	public ProxyRMI(ProxyCli proxy) {
		this.config = new Config("mc");
		this.proxy = proxy;

		try {
			IProxyRMI stub = (IProxyRMI) UnicastRemoteObject.exportObject(this, 0);

			try {
				registry = LocateRegistry.createRegistry(getProxyRMIPort());
			} catch (Exception e) {
				try {
					registry = LocateRegistry.getRegistry(getProxyRMIPort());
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}

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
		if (map.isEmpty()) {
			return new MessageResponse("No downloads yet");
		}

		int size = map.size();

		if (size >= 3) {
			for (int i = 0; i < 3; i++) {
				ret += "\n" + (i + 1) + ". " + findMax(map);
			}
		} else {
			for (int i = 0; i < size; i++) {
				ret += "\n" + (i + 1) + ". " + findMax(map);
			}
			for (int i = 0; i < 3 - size; i++) {
				ret += "\n" + "-";
			}
		}

		return new MessageResponse(ret);
	}

	@Override
	public Response subscribe(String filename, long numberOfDownloads, String username, IClientRMI callbackobject) throws RemoteException {
		proxy.addSubscriber(new Subscriber(filename, numberOfDownloads, callbackobject));
		return new MessageResponse("Succesfully subscribed for file: " + filename);
	}

	@Override
	public PublicKey getProxyPublicKey() throws RemoteException {
		return proxy.getConfigPublicKey();
	}

	@Override
	public Response setUserPublicKey(String username, PublicKey pkey) throws RemoteException {
		boolean bool = proxy.setConfigPublicKey(username, pkey);
		if (bool) {
			return new MessageResponse("Successfully transmitted public key of user: " + username);
		}
		return new MessageResponse("Failed");
	}

	public void close() {
		try {
			UnicastRemoteObject.unexportObject(this, false);
			registry.unbind(getBindingName());
		} catch (NoSuchObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public String getBindingName() {
		return config.getString("binding.name");
	}

	public String getProxyHost() {
		return config.getString("proxy.host");
	}

	public int getProxyRMIPort() {
		return config.getInt("proxy.rmi.port");
	}

	public String getKeysDir() {
		return config.getString("keys.dir");
	}

	public String findMax(Map<String, Integer> map) {
		int max = -1;
		String maxname = "";
		for (String s : map.keySet()) {
			if (map.get(s) > max) {
				max = map.get(s);
				maxname = s;
			}
		}
		map.remove(maxname);

		return maxname + " " + max;
	}

}
