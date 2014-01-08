package your;

import java.util.Timer;
import java.util.TimerTask;

import util.CliComponent;
import util.Config;
import cli.Shell;
import client.ClientCli;

public class TestRunnerClient implements Runnable {

	private ClientCli client;
	private int downloads;
	private int uploads;
	private int size;
	private double ratio;
	private Shell shell;
	private Config config;
	private TimerTask tt;
	private Timer timer;
	private CliComponent component;

	public TestRunnerClient(ClientCli client, Config config, CliComponent comp) {

		this.client = client;
		this.config = config;
		this.component = comp;

		this.downloads = config.getInt("downloadsPerMin");
		this.uploads = config.getInt("uploadsPerMin");
		this.size = config.getInt("fileSizeKB");
		this.ratio = Double.parseDouble(config.getString("overwriteRatio"));
		this.shell = shell;
	}

	@Override
	public void run() {
		tt = new TimerTask() {

			@Override
			public void run() {
				component.getIn().addLine("!login alice 12345");
				component.getIn().addLine("!download short.txt");
			}
		};
		timer = new Timer();
		timer.schedule(tt, 0, 50000000);
	}
}
