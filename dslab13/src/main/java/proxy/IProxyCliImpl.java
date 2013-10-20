package proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import util.Config;

import message.Response;
import message.response.FileServerInfoResponse;
import message.response.MessageResponse;
import message.response.UserInfoResponse;
import model.FileServerInfo;
import model.UserInfo;
import cli.Command;
import cli.Shell;

public class IProxyCliImpl implements IProxyCli {
	
	Config config;
	Shell shell;
	
	FileServerInfoResponse fsi_resp;
	UserInfoResponse ui_resp;
	
	public IProxyCliImpl(Config config,Shell shell){
		this.config = config;
		this.shell = shell;
		fsi_resp = new FileServerInfoResponse(new ArrayList<FileServerInfo>());
		ui_resp = new UserInfoResponse(new ArrayList<UserInfo>());
	}

	@Override
	@Command
	public Response fileservers() throws IOException {
		return fsi_resp;
	}

	@Override
	@Command
	public Response users() throws IOException {
		return ui_resp;
	}

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
