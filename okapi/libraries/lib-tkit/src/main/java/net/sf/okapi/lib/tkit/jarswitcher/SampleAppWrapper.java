package net.sf.okapi.lib.tkit.jarswitcher;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

/**
 * Demonstrates how to use a custom class loader.  
 */
public class SampleAppWrapper {
	
	// java -cp D:\git_local_repo\dev_latest\okapi\okapi\libraries\lib-tkit\target\classes net.sf.okapi.lib.tkit.versions.SampleAppWrapper
	
	public SampleAppWrapper() {
	}
	
    public static void main(String[] args) throws IOException {
         if (args.length == 0) {
        	// !!!
        	 File f = new File(SampleAppWrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        	 
        	 System.out.println("Executing...");
        	 ProcessBuilder pb =
        			 new ProcessBuilder(
        					 "java",
        					 "-cp",
        					 f.getAbsolutePath(),
        					 "-Djava.system.class.loader=net.sf.okapi.lib.tkit.jarswitcher.VMClassLoader", 
        					 SampleAppWrapper.class.getName(),
        					 "QQ");
//        	 File log = new File("C:\\bak\\1\\log.txt");
//        	 pb.redirectOutput(Redirect.appendTo(log));
        	 pb.redirectOutput(Redirect.INHERIT);
        	 pb.start();
        	 
//        	 ProcessBuilder pb =
//        			 new ProcessBuilder(
//        					 "calc");
//        	 pb.start();
        	 
//        	 System.out.println(new File(".").getAbsolutePath());
        	         	         
        	 System.out.println("This class dir: " + f.getAbsolutePath());
//        	 System.out.println(System.getProperty("java.class.path"));
        	 
        	 System.out.println("1");
        	 ClassLoader cl = ClassLoader.getSystemClassLoader();
        	 System.out.println("SysCL: " + cl.getClass().getName());
         }
         else {
        	 System.out.println("\n2");
        	 ClassLoader cl = ClassLoader.getSystemClassLoader();
        	 System.out.println("SysCL: " + cl.getClass().getName());
        	 System.out.println("SysCL parent: " + cl.getParent().getClass().getName());
         }         
    }
}
