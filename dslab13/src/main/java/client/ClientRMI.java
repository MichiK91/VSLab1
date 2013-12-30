package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import proxy.IProxyRMI;

public class ClientRMI {

	public ClientRMI(){
		Registry registry;
		try {
			IProxyRMI stub = (IProxyRMI) Naming.lookup("RMI");
			int res = stub.readQuorum();
			System.out.println(res);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	
}
