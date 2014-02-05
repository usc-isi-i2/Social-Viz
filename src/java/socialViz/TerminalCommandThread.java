package socialViz;

import java.io.IOException;

public class TerminalCommandThread implements Runnable {

	private String[] command;


	public TerminalCommandThread(String... command){
		this.command = command;
	}

	@Override
	public void run() {
		try {

			String strCommand = command[0];
			for(int i=1; i<command.length; i++)
				strCommand = strCommand + " " + command[i];
			//System.out.println("Executing: " + Arrays.toString(command));
			System.out.println("Executing: " + strCommand);

			//Process p = Runtime.getRuntime().exec(new String[]{"bash","-c",strCommand});
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true); 
			Process p = pb.start();

			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),"ERROR");
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(),"OUTPUT");
			outputGobbler.start();
			errorGobbler.start();
			p.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch(InterruptedException ie) {
			ie.printStackTrace();
		}

	}

}
