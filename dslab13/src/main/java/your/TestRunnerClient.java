package your;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import util.CliComponent;
import util.Config;
import client.ClientCli;

public class TestRunnerClient implements Runnable {

	private ClientCli client;
	private int downloads;
	private int uploads;
	private int size;
	private double ratio;
	private Config testConfig;
	private Config clientConfig;
	private TimerTask uploadTimer;
	private TimerTask downloadTimer;
	private Timer timer;
	private CliComponent component;

	public TestRunnerClient(ClientCli client, Config config, CliComponent comp) {

		this.client = client;
		this.testConfig = config;
		this.component = comp;
		clientConfig = new Config("Client");

		this.downloads = config.getInt("downloadsPerMin");
		this.uploads = config.getInt("uploadsPerMin");
		this.size = config.getInt("fileSizeKB");
		this.ratio = Double.parseDouble(config.getString("overwriteRatio"));
	}

	@Override
	public void run() {
		byte[] filedata = new byte[(1024 * size)];
		new Random().nextBytes(filedata);

		String dir = clientConfig.getString("download.dir");

		File file = new File(dir);

		FileOutputStream out;
		try {
			out = new FileOutputStream(dir + "/" + new Random().nextLong());
			out.write(filedata);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		uploadTimer = new TimerTask() {
			@Override
			public void run() {
				component.getIn().addLine("!login alice 12345");
				component.getIn().addLine("!download short.txt");
				component.getIn().addLine("!upload short.txt");
			}
		};
		timer = new Timer();
		timer.schedule(uploadTimer, 0, 50000000);
	}

	public void exit() {
		component.getIn().addLine("!exit");
	}
}
