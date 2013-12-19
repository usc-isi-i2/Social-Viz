package socialViz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Vaishnavi Dalvi
 * Driver class for the project
 */
public class Main {
	
	private static Map<String, Integer> ht = new LinkedHashMap<String, Integer>();
	private static int cooccurence_count=0;
	private static int cumulative_count=0;
	private static 	String base="";

	public static void main(String args[]) throws Exception{
	
		if(args.length != 1){
			System.out.println("USAGE: java Main <Path_of_base_directory_containing_all_resources>");
			System.exit(0);
		}
		
		base=args[0];
		System.out.println(base);
		//createHashmap();
		//createJSONFiles();
		//writeCountsToFile();
		readCountsFromFile();
		generateSVGAndPNG();
		generateVideos();
		
	}
	
	/**
	 * Create hashmap for hashtags in the form <hashtag, identifier>
	 */
	public static void createHashmap(){
		
		hastagHashmap hthm = new hastagHashmap();
		ht = hthm.create();
		
		//Display hashtag list as key-value pair
		/*hthm.display(ht);*/
	
	}
	
	/**
	 * Create daily and cumulative JSON files  
	 */
	public static void createJSONFiles(){
		
		generateCooccurence gc = new generateCooccurence();
		gc.openConnection();
		gc.getDatesFromDatabase();
		gc.buildMatrix(ht);
		gc.closeConnection();
		cooccurence_count=gc.getDaily_max_count();
		cumulative_count=gc.getCum_max_count();
		
		//System.out.println("Maximum daily count over entire period: "+cooccurence_count);
		//System.out.println("Maximum cumulative count: "+cumulative_count);
	}
	
	/**
	 * Write maximum counts for daily and cumulative data to file
	 */
	public static void writeCountsToFile(){
		
		String countFile="C:/Users/Vaishnavi/workspace/social-Viz/resources/maxCount.txt";
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(countFile,"UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.println(cooccurence_count);
		pw.println(cumulative_count);
		pw.close();
	}
	
	/**
	 * Read maximum counts from file
	 */
	public static void readCountsFromFile(){
		
		String countFile=base+"/maxCount.txt";	
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(countFile));
			cooccurence_count = Integer.parseInt(br.readLine());
			cumulative_count = Integer.parseInt(br.readLine());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		System.out.println("Reading counts from file: ");
		System.out.println("Maximum daily count over entire period: "+cooccurence_count);
		System.out.println("Maximum cumulative count: "+cumulative_count);
		
	}
	
	/**
	 * Generate SVG and PNG files from JSON files
	 * @param base : String representing base directory for all resources
	 */
	public static void generateSVGAndPNG(){
		
		String input = base+"/jsonOutput/";
		String outputDirectory="";
		long maxCount = cooccurence_count;
		int count=0;
		 
		File[] files = new File(input).listFiles();
		try {
			showFiles(files, outputDirectory,maxCount,count);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done!");
	}
	
	/**
	 * Generate videos from PNG files for both daily and cumulative data
	 * @param base : String representing base directory for all resources
	 */
	public static void generateVideos(){
	
		String basePng=base+"/png/";
		String baseVideo=base+"/video/";
		
		try {
			generateVideo(basePng+"Cooccurence/",baseVideo+"Cooccurence/out.mp4");
			generateVideo(basePng+"Cumulative/",baseVideo+"Cumulative/out.mp4");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	/**
	 * Recurse directory for JSON data to produce svg and png for every file.
	 * @param files : List of files in the directory
	 * @param outputDirectory : Cooccurence or Cumulative
	 * @param maxCount : Maximum count depending on whether it is daily or cumulative data 
	 * @param count : count for generating png files as a sequence starting with 000.png
	 * @throws Exception
	 */
	public static void showFiles(File[] files, String outputDirectory,long maxCount, int count) throws Exception {
	
		String outputBase = base+"/";
		String svgFile="",pngFile="",fileName="";
		svgTopngConverter s2p = new svgTopngConverter();
			
		Arrays.sort(files);
		for (File file : files) {
			if (file.isDirectory()) {
				if(file.getName().equals("Cooccurence")){
		        	maxCount = cooccurence_count;
		        	count=0;
		        }
		        else{
		        	maxCount = cumulative_count;
		        	count = 0;
		        }	
		
		    outputDirectory = file.getName()+"/";
		    //System.out.println("Directory: " + file.getName());
		    
		    //For generating for only Cumulative data, uncomment the line below
		    //if(file.getName().equals("Cumulative"))
		    showFiles(file.listFiles(), outputDirectory, maxCount,count); 
		    
		    } else {
		        count++;
		        //System.out.println("File: " + file.getAbsolutePath());
		        fileName=file.getName().substring(0,file.getName().indexOf("."));
		        svgFile = outputBase+"svg/"+outputDirectory+fileName+".svg";
		        pngFile = outputBase+"png/"+outputDirectory+String.format("%03d", count)+".png";
		        //System.out.println("Max_count"+maxCount);
		        //System.out.println(svgFile);
		        //System.out.println(pngFile);
		        generate(maxCount,file.getAbsolutePath(),svgFile);
		        s2p.convert(svgFile,pngFile);
		    }
		}
	}
	
	/**
	 * Generate SVG file by calling node script
	 * @param max_count : Maximum count depending on whether it is daily or cumulative data  
	 * @param input : SVG file name
	 * @param output : PNG file name
	 */
	public static void generate(long max_count, String input, String output){
		try{
			 System.out.println("Start--->");
				 
		     String command = "node /home/vdalvi/workspace/social-Viz/src/socialViz/nodeJSDom.js "+max_count+" "+input+" "+output;
		     System.out.println(command);
				 
			 Process p = Runtime.getRuntime().exec(new String[]{"bash","-c",command});
				 
		     StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),"ERROR");
		     StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(),"OUTPUT");
				 
			 outputGobbler.start();
			 errorGobbler.start();
				
			 int exitVal = p.waitFor();
			 System.out.println("Exit value "  + exitVal);
				
		} catch(Throwable t){
			t.printStackTrace();
		}	 
	}
	
	/**
	 * Generate video from png files
	 * @param from : Directory for png files
	 * @param to : Path of output file
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void generateVideo(String from, String to) throws IOException, InterruptedException{
	     
		 String command = "ffmpeg -r 5 -f image2 -i "+from+"%03d.png "+to;
	     System.out.println(command);
			 
		 Process p = Runtime.getRuntime().exec(new String[]{"bash","-c",command});
		 StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),"LOG");
		 errorGobbler.start();	 
		    
		 int exitValue = p.waitFor();
		 System.out.println(exitValue);
	
	}
	
	/**
	 * Create SVG files for files in a directory using a single call to node script
	 * @param base : String representing base directory for all resources 
	 */
	@Deprecated
	public static void createSVGFiles(){
	
		String directory,output;
		long maxCount;
		directory =base+"jsonOutput/Cooccurence";
		output =base+"svg/Cooccurence";
		maxCount = cooccurence_count;
		runScript(maxCount,directory,output);
		
		directory =base+"jsonOutput/Cumulative";
		output =base+"svg/Cumulative";
		maxCount = cumulative_count;
		runScript(maxCount,directory,output);
		
	}
	
	/**
	 * Generate PNG files for all SVG files in a directory
	 * Execute this function after all SVG files have been generated 
	 * (Since JSDom is asynchronous)
	 * @param base : String representing base directory for all resources
	 */
	@Deprecated
	public static void createPNGFiles(){
		
		String directory,output;
		File[] files;
		directory =base+"svg/Cooccurence";
		output =base+"png/Cooccurence";
		files = new File(directory).listFiles();
		Arrays.sort(files);
		try {
			generatePNG(directory,files,output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		directory =base+"svg/Cumulative";
		output =base+"png/Cumulative";
		files = new File(directory).listFiles();
		Arrays.sort(files);
		try {
			generatePNG(directory,files,output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Run node script for a directory
	 * @param maxCount : Maximum count depending on whether it is daily or cumulative data
	 * @param directory : Directory for JSON files
	 * @param output : Directory for SVG files
	 */
	@Deprecated
	public static void runScript(long maxCount,String directory,String output){
		 
		System.out.println("Start--->");
			 
		String command = "node /home/vdalvi/workspace/social-Viz/src/socialViz/nodeJSFinal.js "+maxCount+" "+directory+" "+output;
		System.out.println(command);
			 
		try{
			 Process p = Runtime.getRuntime().exec(new String[]{"bash","-c",command});
			 
			 StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),"ERROR");
			 StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(),"OUTPUT");
			 
			 outputGobbler.start();
			 errorGobbler.start();
			
			 int exitVal = p.waitFor();
			 System.out.println("Exit value "  + exitVal);
			
		 } catch(Throwable t){
			 t.printStackTrace();
		 }	 		 	 
	}
	
	
	/**
	 * 
	 * @param directory : Directory for SVG files
	 * @param files : Array of files in 'directory'
	 * @param outputDirectory : Directory for PNG files
	 * @throws Exception
	 */
	@Deprecated
	public static void generatePNG(String directory, File[] files, String outputDirectory) throws Exception{
		String fileName,svgFile,pngFile;
		svgTopngConverter s2p = new svgTopngConverter();
		int count=1;
		for(File file : files){
		    fileName=file.getName().substring(0,file.getName().indexOf("."));
		    svgFile=directory+"/"+fileName+".svg";
		    pngFile=outputDirectory+"/"+String.format("%03d", count)+".png";
		    s2p.convert(svgFile,pngFile);
		    System.out.println("Done");
		    count++;
		}
	}
}
