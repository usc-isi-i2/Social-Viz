package socialViz;

import java.io.File;
import java.io.IOException;

public class ImageToVideoGenerator {
	AppProperties appProperties;
	
	public ImageToVideoGenerator() throws IOException {
		appProperties = AppProperties.getInstance();
	}
	
	/**
	 * Generate video from png files
	 * @param from : Directory for png files
	 * @param to : Path of output file
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void generateVideo(String from, String to) throws IOException, InterruptedException{
		long time1 = System.currentTimeMillis();
	    //Remove the output file if it exists. 
		File file = new File(to);
	     if(file.exists())
	    	 file.delete();
	     
	     //Noe execute the ffmeg command
		 String command = appProperties.getffmegCommand() 
				 			+ " " + appProperties.getffmegInputOptions() + " -i " + from
				 			+ " " + appProperties.getffmegOutputOptions() + " " + to;
		 
	     System.out.println(command);
			 
		 Process p = Runtime.getRuntime().exec(new String[]{"bash","-c",command});
		 StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),"LOG");
		 errorGobbler.start();	 
		    
		 int exitValue = p.waitFor();
		 System.out.println(exitValue);
		 long time2 = System.currentTimeMillis();
		 System.out.println("Time to generate Video:" + to + ": " + (time2 - time1) + " msec");
	}
}
