package your;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cli.Shell;
import client.ClientCli;

public class TestRunnerClient implements Runnable {

	private ClientCli client;
	private int downloads;
	private int uploads;
	private int size;
	private double ratio;
	private Shell shell;
	private TimerTask tt;
	private Timer timer;

	public TestRunnerClient(ClientCli client, int downloads, int uploads, int size, String ratio, Shell shell) {
		this.client = client;
		this.downloads = downloads;
		this.uploads = uploads;
		this.size = size;
		this.ratio = Double.parseDouble(ratio);
		this.shell = shell;
	}

	@Override
	public void run() {
		tt = new TimerTask() {

			@Override
			public void run() {
				try {
					shell.writeLine("!login alice 2312345");
					shell.writeLine("!download short.txt");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		timer = new Timer();
		timer.schedule(tt, 0, 50000000);
	}
}
