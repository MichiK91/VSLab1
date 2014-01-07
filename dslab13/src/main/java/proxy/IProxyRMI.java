package proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import cli.Command;
import client.ClientRMI;
import client.IClientRMI;

import message.Response;

public interface IProxyRMI extends Remote {
	
	public Response readQuorum() throws RemoteException;
	
	public Response writeQuorum() throws RemoteException;
	
	public Response topThreeDownloads() throws RemoteException;
	
	public Response subscribe(String filename, long numberOfDownloads, String username, IClientRMI callbackobject) throws RemoteException;

	public PublicKey getProxyPublicKey() throws RemoteException;
	
	public Response setUserPublicKey(String username, PublicKey pkey) throws RemoteException;


}