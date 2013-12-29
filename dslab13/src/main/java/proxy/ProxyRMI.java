package proxy;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import message.Response;
import cli.Command;

public class ProxyRMI implements IProxyRMI{
	
	public ProxyRMI(){
		try {
			IProxyRMI stub = (IProxyRMI) UnicastRemoteObject.exportObject(this, 0);
		
		Registry registry = LocateRegistry.getRegistry();
        
			registry.bind("IProxyRMI", stub);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}

	@Override
	@Command
	public int readQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return 5;
	}

	@Override
	@Command
	public Response writeQuorum() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public Response topThreeDownloads() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public Response subscribe(String filename, long numberOfDownloads)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public Response getProxyPublicKey() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public Response setUserPublicKey(String username) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
