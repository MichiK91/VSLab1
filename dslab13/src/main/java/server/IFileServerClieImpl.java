package server;

import java.io.IOException;

import message.response.MessageResponse;
import cli.Command;

public class IFileServerClieImpl implements IFileServerCli {

	@Override
	@Command
	public MessageResponse exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
