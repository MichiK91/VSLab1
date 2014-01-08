package your;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
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
	private Timer timer2;
	private CliComponent component;
	private int number;

	public TestRunnerClient(ClientCli client, Config config, CliComponent comp, int number) {

		this.client = client;
		this.testConfig = config;
		this.component = comp;
		clientConfig = new Config("Client");

		this.downloads = config.getInt("downloadsPerMin");
		this.uploads = config.getInt("uploadsPerMin");
		this.size = config.getInt("fileSizeKB");
		this.ratio = Double.parseDouble(config.getString("overwriteRatio"));
		
		this.number=number;
	}

	@Override
	public void run() {
	  
	  component.getIn().addLine("!login alice"+number+" 12345");

	  //Downloadtest
    downloadTimer = new TimerTask() {
		@Override
			public void run() {
				component.getIn().addLine("!download short.txt");
			}
		};
		timer = new Timer();
		timer.schedule(downloadTimer, (new Random()).nextInt(60000/downloads), 60000/downloads);
		
		
		//Uploadtest
		byte[] filedata = new byte[(1024 * size)];
    new Random().nextBytes(filedata);
    
    String dir = clientConfig.getString("download.dir");

    File file = new File(dir);

    FileOutputStream out;
    final String existingFile = "existing.txt";
    
    try {
      out = new FileOutputStream(dir + "/" +existingFile);
      
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
        
        Random random = new Random();
        if(random.nextDouble()<ratio){
          
          component.getIn().addLine("!upload "+existingFile);
        
        } else {
          
          byte[] filedata = new byte[(1024 * size)];
          new Random().nextBytes(filedata);
          String dir = clientConfig.getString("download.dir");
          File file = new File(dir);

          FileOutputStream out;
          try {
            final String newFile = ""+new Random().nextLong();
            out = new FileOutputStream(dir + "/" +newFile);
            out.write(filedata);
            component.getIn().addLine("!upload "+newFile);
            out.close();
          } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          
        }
      }
    };
    timer2 = new Timer();
    timer2.schedule(uploadTimer, (new Random()).nextInt(60000/uploads), 60000/uploads);

	}

	public void exit() {
		component.getIn().addLine("!exit");
	}
}
