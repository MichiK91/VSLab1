package server;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import util.Config;

import message.Response;
import message.request.*;
import message.response.*;

public class FileServerImpl implements IFileServer, Closeable {

	private Config config;
	private ProxySenderUDP sender;

	public FileServerImpl(Config config) {
		this.config = config;
		this.sender = new ProxySenderUDP(this.config);

	}

	@Override
	public Response list() throws IOException {
		File file = new File(config.getString("fileserver.dir"));
		File[] files = file.listFiles();

		Set<String> names = new HashSet<String>();
		for (File f : files) {
			if (f.isFile()) {
				names.add(f.getName());
			}
		}
		return new ListResponse(names);
	}

	@Override
	public Response download(DownloadFileRequest request) throws IOException {
		File file = new File(config.getString("fileserver.dir"));
		File[] files = file.listFiles();
		byte[] content = null;

		for (File f : files) {
			if (f.isFile()) {
				if (f.getName().equals(request.getTicket().getFilename())) {
					// read content
					FileInputStream in = new FileInputStream(f);
					String s = "";
					while (true) {
						int read = in.read();
						if (read == -1) {
							break;
						} else {
							char c = (char) read;
							s += c;
						}
					}
					content = s.getBytes();
					in.close();
				}
			}
		}
		return new DownloadFileResponse(request.getTicket(), content);
	}

	@Override
	public Response info(InfoRequest request) throws IOException {
		String name = request.getFilename();
		File file = new File(config.getString("fileserver.dir"));
		File[] files = file.listFiles();
		long size = 0;

		for (File f : files) {
			if (f.isFile()) {
				if (f.getName().equals(request.getFilename())) {
					size = f.length();
				}
			}
		}

		return new InfoResponse(name, size);
	}

	@Override
	public Response version(VersionRequest request) throws IOException {
		String name = request.getFilename();
		File file = new File(config.getString("fileserver.dir"));
		File[] files = file.listFiles();
		int version = 0;

		for (File f : files) {
			if (f.isFile()) {
				if (f.getName().equals(request.getFilename())) {
					version = 1;
				}
			}
		}
		return new VersionResponse(name, version);
	}

	@Override
	public MessageResponse upload(UploadRequest request) throws IOException {
		
		// first delete existing file
		String dir = config.getString("fileserver.dir");
		File file = new File(dir);
		File[] files = file.listFiles();

		for (File f : files) {
			if (f.isFile()) {
				if (f.getName().equals(request.getFilename())) {
					f.delete();
				}
			}
		}
		
		FileOutputStream out = new FileOutputStream(dir + "/"
				+ request.getFilename());
		out.write(request.getContent());
		out.close();
		return new MessageResponse("successfully uploaded");
	}

	@Override
	public void close() throws IOException {
		sender.close();
	}

}
