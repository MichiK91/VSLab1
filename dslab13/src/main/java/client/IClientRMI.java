package client;

import java.rmi.RemoteException;

import cli.Command;
import message.Response;

public interface IClientRMI {
	
	@Command
	public Response readQuorum() throws RemoteException;
	
	@Command
	public Response writeQuorum() throws RemoteException;
	
	@Command
	public Response topThreeDownloads() throws RemoteException;
	
	
	@Command
	public Response subscribe(String filename, int numberOfDownloads) throws RemoteException;
	
	@Command
	public Response getProxyPublicKey() throws RemoteException; 
	
	@Command
	public Response setUserPublicKey(String username) throws RemoteException;
	
}
