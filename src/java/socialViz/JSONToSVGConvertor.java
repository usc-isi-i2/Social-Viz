package socialViz;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JSONToSVGConvertor {

	private AppProperties appProperties;
	private int maxCount;
	private File[] inputFiles;
	private String outputPath;

	public JSONToSVGConvertor(String inputPath, String outputPath, int maxCount) throws IOException {

		appProperties = AppProperties.getInstance();
		this.maxCount = maxCount;
		this.outputPath = outputPath;
		this.inputFiles = getAllFiles(new File(inputPath));
		Arrays.sort(this.inputFiles);
	}


	public void generateSVGForFile(String inputFilename, String outputFilename){
		long time1 = System.currentTimeMillis();
		try{
			String command = getConvertCommand(inputFilename, outputFilename);

			Process p = Runtime.getRuntime().exec(new String[]{"bash","-c",command});

			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),"ERROR");
			//StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(),"OUTPUT");

			//outputGobbler.start();
			errorGobbler.start();

			p.waitFor();
			
		} catch(Throwable t){
			t.printStackTrace();
		}	 
		long time2 = System.currentTimeMillis();
		 System.out.println("Time to generate SVG:" + outputFilename + ": " + ((double)(time2 - time1)) /1000 + " sec");
	}

	private String getConvertCommand(String inputFilename, String outputFilename) {
		String command = appProperties.getNodeCommand() + " " + appProperties.getNodeScript() + " "
				+ maxCount + " " + inputFilename + " " + outputFilename;
		return command;
	}
	
	private File[] getAllFiles(File path) {
		if(path.isDirectory()) {
			return path.listFiles();
		}
		return new File[] {path};
	}

	public void convert() {
		ExecutorService executor = Executors.newFixedThreadPool(appProperties.getNumThreads());
		long time1 = System.currentTimeMillis();
		for(File file : inputFiles) {
			String fileName = file.getName();

			if(fileName.endsWith(".json")) {
				fileName = file.getName().substring(0,file.getName().indexOf("."));
				String svgFile = outputPath + "/" + fileName+".svg";
				//String command = getConvertCommand(file.getAbsolutePath(), svgFile);
				TerminalCommandThread terminalThread = new TerminalCommandThread(appProperties.getNodeCommand(), 
						appProperties.getNodeScript(), maxCount + "", file.getAbsolutePath(), svgFile);
				executor.execute(terminalThread);
			}
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		long time2 = System.currentTimeMillis();
		 System.out.println("Time to generate all SVG files:" + outputPath + ": " + ((double)(time2 - time1)) /(1000 * 60) + " mins");
		
	}
	
	
}
