package server;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Config;

import message.Response;
import message.request.*;
import message.response.*;

public class FileServerImpl implements IFileServer, Closeable {
	
	private Config config;
	private ProxySenderUDP sender;
	private String dir;
	
	public FileServerImpl(Config config) throws SocketException{
		this.config = config;
		this.sender = new ProxySenderUDP(this.config);
	}

	@Override
	public Response list() throws IOException {
		File file = new File(config.getString("fileserver.dir"));
		File[] files = file.listFiles();
		  
        Set<String> names = new HashSet<String>();
        for (File f: files) {
            if (f.isFile()) {
               names.add(f.getName());
            }
        }
        return new ListResponse(names); 
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
