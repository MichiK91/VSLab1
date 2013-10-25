package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;

import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.MessageResponse;

public class FileServerImpl implements IFileServer, Closeable {
	
	private Config config;
	private ProxySenderUDP sender;
	
	public FileServerImpl(Config config) throws SocketException{
		this.config = config;
		this.sender = new ProxySenderUDP(this.config);
	}

	@Override
	public Response list() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		sender.close();
	}

}
