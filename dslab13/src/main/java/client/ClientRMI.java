package client;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;

import message.Response;
import message.response.MessageResponse;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

import proxy.IProxyRMI;
import util.Config;
import cli.Command;

public class ClientRMI extends UnicastRemoteObject implements IClientRMI, Serializable {

	private Config config, cliconfig;
	private ClientCli client;
	private IProxyRMI stub;
	private Registry registry;

	public ClientRMI(ClientCli client) throws RemoteException {
		this.config = new Config("mc");
		this.cliconfig = new Config("client");
		this.client = client;

		// init
		try {
			registry = LocateRegistry.getRegistry(getProxyHost(), getProxyRMIPort());
			stub = (IProxyRMI) registry.lookup(getBindingName());
		} catch (Exception e1) {
			System.err.println("No proxy available.");
		}
	}

	@Override
	@Command
	public Response readQuorum() throws RemoteException {
		return stub.readQuorum();
	}

	@Override
	@Command
	public Response writeQuorum() throws RemoteException {
		return stub.writeQuorum();
	}

	@Override
	@Command
	public Response topThreeDownloads() throws RemoteException {
		return stub.topThreeDownloads();
	}

	@Override
	@Command
	public Response subscribe(String filename, long numberOfDownloads) throws RemoteException {
		if (!client.isLogin())
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
	public Response getProxyPublicKey() throws RemoteException {
		PublicKey key = stub.getProxyPublicKey();
		String path = config.getString("keys.dir");

		try {
			PEMWriter out = new PEMWriter(new FileWriter(path + "/proxy.pub.pem"));
			out.writeObject(key);
			out.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MessageResponse("Successfully received public key of Proxy.");
	}

	@Override
	@Command
	public Response setUserPublicKey(String username) throws RemoteException {
		String path = cliconfig.getString("keys.dir");
		PublicKey pkey = null;
		try {
			PEMReader in = new PEMReader(new FileReader(path + "/" + username.toLowerCase() + ".pub.pem"));
			pkey = (PublicKey) in.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stub.setUserPublicKey(username, pkey);
		// return new MessageResponse("Successfully transmitted public key of user: " + username);
	}

	@Command
	public Response test() {
		String path = cliconfig.getString("key.dir");
		try {
			PEMReader in = new PEMReader(new FileReader(path + "/" + "alice" + ".pub.pem"));
			PublicKey pkey = (PublicKey) in.readObject();
			return new MessageResponse("" + pkey);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void notify(String filename, long numberOfDownloads) throws RemoteException {
		client.notify(filename, numberOfDownloads);
	}

	public void close() {
		try {
			UnicastRemoteObject.unexportObject(this, false);
		} catch (RemoteException e) {
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

}
