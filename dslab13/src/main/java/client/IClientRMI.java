package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import cli.Command;
import message.Response;

public interface IClientRMI extends Remote {
	
	@Command
	public Response readQuorum() throws RemoteException;
	
	@Command
	public Response writeQuorum() throws RemoteException;
	
	@Command
	public Response topThreeDownloads() throws RemoteException;
	
	
	@Command
	public Response subscribe(String filename, long numberOfDownloads) throws RemoteException;
	
	@Command
	public Response getProxyPublicKey() throws RemoteException; 
	
	@Command
	public Response setUserPublicKey(String username) throws RemoteException;
	
	public void notify(String filename, long numberOfDownloads) throws RemoteException;
	
}
