package socialViz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Vaishnavi Dalvi
 * Driver class for the project
 */
public class Main {
	
	private static int cooccurence_count=0;
	private static int cumulative_count=0;
	private static 	String base="";
	private static AppProperties appProperties;
	
	public static void main(String args[]) throws Exception{
		appProperties = AppProperties.getInstance();
		base = appProperties.getOutputFolder();
		
		System.out.println(base);
		
		readCountsFromFile();
		createAllDirectories();
		
		JSONToSVGConvertor cooccuranceConvertor = new JSONToSVGConvertor(base + "/jsonOutput/Cooccurence", base + "/svg/Cooccurence", cooccurence_count);
		cooccuranceConvertor.convert();
		
		JSONToSVGConvertor cumulativeConvertor = new JSONToSVGConvertor(base + "/jsonOutput/Cumulative", base + "/svg/Cumulative", cumulative_count);
		cumulativeConvertor.convert();
		
		SVGtoPNGConvertor svgToPng = new SVGtoPNGConvertor();
		svgToPng.convertDirectory(base + "/svg/Cooccurence", base + "/png/Cooccurence");
		svgToPng.convertDirectory(base + "/svg/Cumulative", base + "/png/Cumulative");
		
		ImageToVideoGenerator videoGen = new ImageToVideoGenerator();
		videoGen.generateVideo(base + "/png/Cooccurence/%03d.png", base + "/video/Cooccurence/out.mp4");
		videoGen.generateVideo(base + "/png/Cumulative/%03d.png", base + "/video/Cumulative/out.mp4");
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
	
		System.out.println("Reading counts from file: " + countFile);
		System.out.println("Maximum daily count over entire period: "+cooccurence_count);
		System.out.println("Maximum cumulative count: "+cumulative_count);
		
	}
	
	public static void createAllDirectories() {
		//Create missing svg folders
		File f = new File(base + "/svg");
		if(!f.exists())
			f.mkdir();
		f = new File(base + "/svg/Cooccurence");
		if(!f.exists())
			f.mkdir();
		f = new File(base + "/svg/Cumulative");
		if(!f.exists())
			f.mkdir();
		
		//Create missing png folders
		f = new File(base + "/png");
		if(!f.exists())
			f.mkdir();
		f = new File(base + "/png/Cooccurence");
		if(!f.exists())
			f.mkdir();
		f = new File(base + "/png/Cumulative");
		if(!f.exists())
			f.mkdir();
		
		//Create missing video folders
		f = new File(base + "/video");
		if(!f.exists())
			f.mkdir();
		f = new File(base + "/video/Cooccurence");
		if(!f.exists())
			f.mkdir();
		f = new File(base + "/video/Cumulative");
		if(!f.exists())
			f.mkdir();
	}
	
	
	
}
