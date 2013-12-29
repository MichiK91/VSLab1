package proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;

import cli.Command;

import message.Response;

public interface IProxyRMI extends Remote {
	
	@Command
	int readQuorum() throws RemoteException;
	
	@Command
	Response writeQuorum() throws RemoteException;
	
	@Command
	Response topThreeDownloads() throws RemoteException;
	
	@Command
	Response subscribe(String filename, long numberOfDownloads) throws RemoteException;

	@Command
	Response getProxyPublicKey() throws RemoteException;
	
	@Command
	Response setUserPublicKey(String username) throws RemoteException;
}
