package your;

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
	private Config config;
	private TimerTask tt;
	private Timer timer;
	 private TimerTask tt2;
	  private Timer timer2;
	private CliComponent component;

	public TestRunnerClient(ClientCli client, Config config, CliComponent comp, int number) {

		this.client = client;
		this.config = config;
		this.component = comp;

		this.downloads = config.getInt("downloadsPerMin");
		this.uploads = config.getInt("uploadsPerMin");
		this.size = config.getInt("fileSizeKB");
		this.ratio = Double.parseDouble(config.getString("overwriteRatio"));
	}

	@Override
	public void run() {
	  component.getIn().addLine("!login alice 12345");
		tt = new TimerTask() {

			@Override
			public void run() {
				component.getIn().addLine("!download short.txt");
			}
		};
		timer = new Timer();
		timer.schedule(tt, 0, downloads);
		

    tt2 = new TimerTask() {

      @Override
      public void run() {
        component.getIn().addLine("!upload short.txt");
      }
    };
    timer2 = new Timer();
    timer2.schedule(tt2, 0, uploads);
	}

	public void exit() {
		component.getIn().addLine("!exit");
	}
}
