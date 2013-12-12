package socialViz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author Vaishnavi Dalvi
 *
 */
public class Main {
	
	private static Map<String, Integer> ht = new LinkedHashMap<String, Integer>();
	private static int cooccurence_count=0;
	private static int cumulative_count=0;
	
	public static void main(String args[]) throws Exception{
		
		String countFile="";
		
		
		/*
		//Uncomment when creating json files
		//Create hashmap for hashtags
		hastagHashmap hthm = new hastagHashmap();
		ht = hthm.create();
		*/
			
		//Display hashtag list as key-value pair
		/*hthm.display(ht);*/
		
		/*
		//Uncomment when creating json files
		//Create json files 
		generateCooccurence gc = new generateCooccurence();
		gc.openConnection();
		gc.getDatesFromDatabase();
		gc.buildMatrix(ht);
		gc.closeConnection();
		cooccurence_count=gc.getDaily_max_count();
		cumulative_count=gc.getCum_max_count();
		System.out.println(cooccurence_count);
		System.out.println(cumulative_count);
		
		//Write max cooccurence and cumulative counts to file, so that generateOccurrence need not be run every time
		countFile="C:/Users/Vaishnavi/workspace/social-Viz/resources/maxCount.txt";
		PrintWriter pw = new PrintWriter(countFile,"UTF-8");
		pw.println(cooccurence_count);
		pw.println(cumulative_count);
		pw.close();
		*/
		
		//Read counts from file
		countFile="/home/vdalvi/workspace/social-Viz/resources/maxCount.txt";	
		BufferedReader br = new BufferedReader(new FileReader(countFile));
		cooccurence_count = Integer.parseInt(br.readLine());
		cumulative_count = Integer.parseInt(br.readLine());
		
		System.out.println(cooccurence_count);
		System.out.println(cumulative_count);
		
		String base="/home/vdalvi/workspace/social-Viz/resources/";
		/*
		//Uncomment this when creating svg and png files
		//Generate svg and png files for json output
		String input = base+"jsonOutput/";
		String outputDirectory="";
		long maxCount = cooccurence_count;
		int count=0;
		 
		File[] files = new File(input).listFiles();
		showFiles(files, outputDirectory,maxCount,count);
		System.out.println("Done!");
		*/
		
		
		/*
		//Deprecated block
		//Generate svg files.
		String directory,output;
		long maxCount;
		directory =base+"jsonOutput/Cooccurence";
		output =base+svg/Cooccurence";
		maxCount = cooccurence_count;
		runScript(maxCount,directory,output);
		
		directory =base+"jsonOutput/Cumulative";
		output =base+"svg/Cumulative";
		maxCount = cumulative_count;
		runScript(maxCount,directory,output);
		*/
		
		/*
		//Deprecated block
		//Generate png files after all svg files have been generated
		File[] files;
		directory =base+"svg/Cooccurence";
		output =base+"png/Cooccurence";
		files = new File(directory).listFiles();
		Arrays.sort(files);
		generatePNG(directory,files,output);

		directory =base+"svg/Cumulative";
		output =base+"png/Cumulative";
		files = new File(directory).listFiles();
		Arrays.sort(files);
		generatePNG(directory,files,output);
		*/
		
		/*
		//Uncomment when creating a video
		//Create a video
		String basePng=base+"png/";
		String baseVideo=base+"video/";
		
		//generateVideo(basePng+"Cooccurence/",baseVideo+"Cooccurence/out.mp4");
		generateVideo(basePng+"Cumulative/",baseVideo+"Cumulative/out.mp4");
		*/
	}
	
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
	
	public static void generateVideo(String from, String to) throws IOException, InterruptedException{
	     String command = "ffmpeg -r 5 -f image2 -i "+from+"%03d.png "+to;
	     System.out.println(command);
			 
		 Process p = Runtime.getRuntime().exec(new String[]{"bash","-c",command});
		 StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),"LOG");
		 errorGobbler.start();	 
		    
		 int exitValue = p.waitFor();
		 System.out.println(exitValue);
	
	}
	
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
	
	public static void showFiles(File[] files, String outputDirectory,long maxCount, int count) throws Exception {
			//String outputBase = "/home/vdalvi/workspace/svgFromd3/resources/svg/";
		String outputBase = "/home/vdalvi/workspace/social-Viz/resources/";
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
		    //if(file.getName().equals("Cumulative"))
		    showFiles(file.listFiles(), outputDirectory, maxCount,count); // Calls same method again.
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
		
}
