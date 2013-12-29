package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import proxy.IProxyRMI;

public class ClientRMI {

	public ClientRMI(){
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry("proxy.ProxyRMI");
			IProxyRMI stub = (IProxyRMI) registry.lookup("IProxyRMI");
			int res = stub.readQuorum();
			System.out.println(res);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
