package socialViz;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Adapted from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 * Prints output stream and error stream of process on the console as separate threads
 */
public class StreamGobbler extends Thread {

	InputStream is;
	String type;
	
	public StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}
	
	
	public void run(){
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			try{
				while((line = br.readLine()) != null) {
					if(!type.equals("OUTPUT"))
						System.out.println(type + ">" + line);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
	}
}

