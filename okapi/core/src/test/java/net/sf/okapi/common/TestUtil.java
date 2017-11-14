package net.sf.okapi.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Class to hold test utility methods
 * @author Christian Hargraves
 *
 */
public class TestUtil {
    /**
     * Takes a class and a file path and returns you the parent directory name of that file. This is used
     * for getting the directory from a file which is in the classpath.
     * @param clazz - The class to use for classpath loading. Don't forget the resource might be a jar file where this
     * class exists.
     * @param filepath - the location of the file. For example &quot;/testFile.txt&quot; would be loaded from the root
     * of the classpath.
     * @return The path of directory which contains the file
     */
	@SuppressWarnings("rawtypes")
	public static String getParentDir(Class clazz, String filepath) {
        URL url = clazz.getResource(filepath);
        String parentDir = null;
        if (url != null) {
			try {
				File file = new File(url.toURI());
				parentDir = Util.ensureSeparator(file.getParent(), true);
			} catch (URISyntaxException e) {
				return null;
			}
        }
        return parentDir;
    }
	
    public static String getFileAsString(final File file) throws IOException {
        try (final BOMAwareInputStream bis = new BOMAwareInputStream(new FileInputStream(file), "UTF-8")) {
        	return StreamUtil.streamAsString(bis, bis.detectEncoding());
        }        
    }
    
    public static void writeString(String str, String filePath, String encoding) throws java.io.IOException{
		if (Util.isEmpty(encoding))
			encoding = Charset.defaultCharset().name();
        FileOutputStream fos = new FileOutputStream(filePath); 
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, encoding));
        writer.write(str);
        writer.close();
        fos.close();
    }
    
    public static String inputStreamAsString(InputStream is) throws IOException {
		StringBuffer out = new StringBuffer();
	    byte[] b = new byte[4096];
	    for (int n; (n = is.read(b)) != -1;) {
	        out.append(new String(b, 0, n));
	    }
	    return out.toString();
	}
}
