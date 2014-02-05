package socialViz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * Adapted from http://thinktibits.blogspot.com/2012/12/Batik-Convert-SVG-PNG-Java-Program-Example.html
 * Converts SVG file to PNG file using Batik
 * 
 */
public class SVGtoPNGConvertor {
	
	public void convertDirectory(String fromPath, String toPath) throws Exception {
		File[] fromFiles = (new File(fromPath)).listFiles();
		int count = 0;
		
		ExecutorService executor = Executors.newFixedThreadPool(AppProperties.getInstance().getNumThreads());
		long time1 = System.currentTimeMillis();
		
		for(File file : fromFiles) {
			String filename = file.getName();
			if(filename.endsWith(".svg")) {
				String svgFile = file.getAbsolutePath();
				String pngFile = toPath + "/" + String.format("%03d", count)+".png";
				SVGToPNGConvertorThread terminalThread = new SVGToPNGConvertorThread(svgFile, pngFile);
				executor.execute(terminalThread);
				count++;
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
		 System.out.println("Time to generate all PNG files:" + toPath + ": " + ((double)(time2 - time1)) /(1000 * 60) + " mins");
	}
	
	
	public void convertFile(String from, String to) throws Exception {
		long time1 = System.currentTimeMillis();
        new SVGToPNGConvertorThread(from, to).run();
        long time2 = System.currentTimeMillis();
        System.out.println("Time to generate PNG:" + to + ": " + ((double)(time2 - time1)) /1000 + " sec");
    }

	private class SVGToPNGConvertorThread implements Runnable {
		private String from, to;
		
		public SVGToPNGConvertorThread(String from, String to) {
			this.from = from;
			this.to = to;
		}
		
		@Override
		public void run() {
			try {
				String svg_URI_input = new File(from).toURI().toURL().toString();
		        TranscoderInput input_svg_image = new TranscoderInput(svg_URI_input);        
		        OutputStream png_ostream = new FileOutputStream(to);
		        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);              
		        PNGTranscoder my_converter = new PNGTranscoder();        
		        my_converter.transcode(input_svg_image, output_png_image);
		        png_ostream.flush();
		        png_ostream.close();  
		        input_svg_image = null;
		        output_png_image = null;
			} catch(IOException ie) {
				ie.printStackTrace();
			} catch (TranscoderException te) {
				te.printStackTrace();
			}
		}
		
	}
}
