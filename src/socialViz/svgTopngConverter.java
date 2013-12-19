package socialViz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * Adapted from http://thinktibits.blogspot.com/2012/12/Batik-Convert-SVG-PNG-Java-Program-Example.html
 * Converts SVG file to PNG file using Batik
 * 
 */
public class svgTopngConverter {
	public void convert(String from, String to) throws Exception {
        String svg_URI_input = new File(from).toURI().toURL().toString();
        TranscoderInput input_svg_image = new TranscoderInput(svg_URI_input);        
        OutputStream png_ostream = new FileOutputStream(to);
        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);              
        PNGTranscoder my_converter = new PNGTranscoder();        
        my_converter.transcode(input_svg_image, output_png_image);
        png_ostream.flush();
        png_ostream.close();        
    }

}
