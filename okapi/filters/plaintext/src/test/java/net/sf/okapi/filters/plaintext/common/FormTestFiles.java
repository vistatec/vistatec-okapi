package net.sf.okapi.filters.plaintext.common;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.FileLocation;


//TODO: Change this so it's used for debug but not unit tests
public class FormTestFiles {
	private static FileLocation location = FileLocation.fromClass(FormTestFiles.class);

	private static void formTestFile(String fileName, String lineBreak, boolean lineBreakAtFileEnd) {				
		try {
			OutputStream st = location.out(fileName).asOutputStream();
			OutputStreamWriter out = new OutputStreamWriter(st, "UTF-8");
	        
	        out.write("Line 1"); out.write(lineBreak);
	        out.write("Line 2"); out.write(lineBreak);
	        out.write("Line 3"); out.write(lineBreak);
	        out.write("Line 4"); 
	        
	        if (lineBreakAtFileEnd) out.write(lineBreak);
	        
	        out.close();
	    } catch (IOException e) {
	    	
	    }
	}
	
	private static void formMixture() {				
		try {
			OutputStream st = location.out("mixture.txt").asOutputStream();
			OutputStreamWriter out = new OutputStreamWriter(st, "UTF-8");
	        
	        out.write("Line 1"); out.write("\r\n");
	        out.write("Line 2"); out.write("\u2028");
	        out.write("Line 3"); out.write("\n");
	        out.write("Line 4"); out.write("\n\r");
	        
	        out.close();
	    } catch (IOException e) {
	    	
	    }
	}
	
	@SuppressWarnings("unused")
	private void appendToFile(String fileName, String st) {
		try {
	        File file = location.out(fileName).asFile();
	        BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
	        out.write(st);
	        out.close();
	    } catch (IOException e) {}
    }

	public static void main(String[] args) {
		formTestFile("crlf.txt", "\r\n", false);
		formTestFile("lf.txt", "\n", false); 
		formTestFile("cr.txt", "\r", false);
		formTestFile("u0085.txt", "\u0085", false);
		formTestFile("u2028.txt", "\u2028", false);
		formTestFile("u2029.txt", "\u2029", false);
		formTestFile("crlfcrlf.txt", "\r\n\r\n", false);		
		formTestFile("custom.txt", "(custom)", false);
		
		formTestFile("crlf_end.txt", "\r\n", true);
		formTestFile("crlfcrlf_end.txt", "\r\n\r\n", true);
		
		formMixture();
		
		Logger localLogger = LoggerFactory.getLogger(FormTestFiles.class);
		localLogger.debug("Done");
	}

}
