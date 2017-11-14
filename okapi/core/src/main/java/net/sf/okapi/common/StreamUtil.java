/*===========================================================================
  Copyright (C) 2010-2014 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPInputStream;

import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.io.FileCachedInputStream;

/**
 * Various manipulations of streams, files, etc. Most methods rewrap IOException as
 * OkapiIOException.
 */
public class StreamUtil {

	/**
	 * Reads an InputStream into an array of bytes.
	 * @param in the input stream to read.
	 * @return the array of bytes read.
	 * @throws IOException if an error occurs.
	 */
	public static byte[] inputStreamToBytes(InputStream in) throws IOException
	{
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
			byte[] buffer = new byte[1024];
			int len;

			while((len = in.read(buffer)) >= 0) {
				out.write(buffer, 0, len);
			}
			return out.toByteArray();
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	public static void copy(File in, OutputStream out) {
		try {
			Files.copy(in.toPath(), out);
		} catch (FileNotFoundException e) {
			throw new OkapiFileNotFoundException(e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	public static String streamAsString(InputStream in, String encoding) {
		try (Scanner s = new Scanner(in, encoding).useDelimiter("\\A")) {
			String tmp = s.hasNext() ? s.next() : "";
			return Util.normalizeNewlines(tmp.toString());
		}
	}

	public static String streamUtf8AsString(InputStream in) {
		return streamAsString(in, "UTF-8");
	}
	
	public static InputStream stringAsStream(String str, String encoding) {
		try {
			return new ByteArrayInputStream(str.getBytes(encoding));
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	public static long calcCRC(InputStream in) {
		CheckedInputStream cis = new CheckedInputStream(in, new CRC32());
		byte[] buf = new byte[1024];
        try {
			while(cis.read(buf) >= 0) {
			}
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
        return cis.getChecksum().getValue();
	}

	/**
	 * Copies a {@link ReadableByteChannel} to a {@link WritableByteChannel}.
	 * @param inChannel the input Channel.
	 * @param outChannel the output Channel.
	 * @throws OkapiIOException if an error occurs.
	 */
	public static void copy(ReadableByteChannel inChannel, WritableByteChannel outChannel)
	{
		 copy(inChannel, outChannel, true);
	}
	
	/**
	 * Copies a {@link ReadableByteChannel} to a {@link WritableByteChannel}.
	 * @param inChannel the input Channel.
	 * @param outChannel the output Channel.
	 * @param closeChannels close in and out channels? 
	 * @throws OkapiIOException if an error occurs.
	 */
	public static void copy(ReadableByteChannel inChannel, WritableByteChannel outChannel, boolean closeChannels)
	{
		// allocateDirect may be faster, but the risk of 
		// out of memory and memory leak is higher
		// see http://www.informit.com/articles/article.aspx?p=2133373&seqNum=12
		ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
		try {
			while (inChannel.read(buffer) != -1) {
				// prepare the buffer to be drained
				buffer.flip();
				// write to the channel, may block
				outChannel.write(buffer);
				// If partial transfer, shift remainder down
				// If buffer is empty, same as doing clear()
				buffer.compact();
			}
	
			// EOF will leave buffer in fill state
			buffer.flip();
			// make sure the buffer is fully drained.
			while (buffer.hasRemaining()) {
				outChannel.write(buffer);
			}
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		} finally {
			buffer.clear();
			buffer = null;
			try {
				if (inChannel != null && closeChannels) inChannel.close();
			} catch (IOException e) {
				throw new OkapiIOException(e);
			}
			try {
				// don't close FileCachedInputStream as this
				// resets the position and must call reOpen to use it
				if (outChannel != null 
						&& !(outChannel instanceof FileCachedInputStream)
						&& closeChannels) {
					outChannel.close();
				}
			} catch (IOException e) {			
				throw new OkapiIOException(e);
			}
		}
	}

	
	/**
	 * Copies an {@link InputStream} to a File.
	 * @param in the input stream.
	 * @param outputFile the output {@link File}.
	 * @throws OkapiIOException if an error occurs.
	 */
	public static void copy(InputStream in, File outputFile) {
		try {
			if (!outputFile.exists()) {
				Util.createDirectories(outputFile.getAbsolutePath());
				outputFile.createNewFile();
			}
			Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	/**
	 * Copies an {@link InputStream} to a File.
	 * @param is the input stream.
	 * @param outputPath the output path.
	 * @throws OkapiIOException if an error occurs.
	 */
	public static void copy(InputStream is, String outputPath)
	{
		try {
			Files.copy(is, Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		}
	}
	
	/**
	 * Copies an {@link InputStream} to an {@link OutputStream}.
	 * @param is the input stream.
	 * @param outputStream the output stream.
	 * @throws OkapiIOException if an error occurs.
	 */
	public static void copy(InputStream is, OutputStream outputStream)
	{
		ReadableByteChannel inChannel = null;
		WritableByteChannel outChannel = null;
		try {
			inChannel = Channels.newChannel(is);
			outChannel = Channels.newChannel(outputStream);
			// channels are closed here
			copy(inChannel, outChannel);
		}
		finally {
			try {
				if (is != null) is.close();
				if (outputStream != null) outputStream.close();
			}
			catch ( IOException e ) {
				throw new OkapiIOException(e);
			}
		}
	}

	/**
	 * Copies one file to another.
	 * @param in the input file.
	 * @param out the output file.
	 * @throws OkapiIOException if an error occurs.
	 */
	public static void copy(File in, File out)
	{
		try {
			Files.copy(in.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		}
	}

	/**
	 * Copies a file from one location to another.
	 * @param fromPath the path of the file to copy.
	 * @param toPath the path of the copy to make.
	 * @param move true to move the file, false to copy it.
	 * @throws OkapiIOException if an error occurs.
	 */
	public static void copy(String fromPath, String toPath, boolean move)
	{
		try {
			Util.createDirectories(toPath);
			Files.copy(Paths.get(fromPath), Paths.get(toPath), StandardCopyOption.REPLACE_EXISTING);
		}
		catch ( IOException e ) {			
			throw new OkapiIOException(e);
		}
	}
	
	public static FileCachedInputStream createResettableStream(InputStream is, int bufferSize) throws IOException {
		FileCachedInputStream ifcis;
		try {
			ifcis = new FileCachedInputStream(bufferSize);
			copy(Channels.newChannel(is), ifcis);
		} catch (IOException e) {
			is.close();
			throw new OkapiIOException("Error copying inputstream to FileCachedInputStream", e);
		} finally {
			is.close();
		}
		
		// create a mark on the first byte
		// this is expected by some callers
		// that reset the stream themselves
		ifcis.mark(Integer.MAX_VALUE);
		return ifcis;
	}
	
	/*
	 * Determines if a byte array is compressed. The java.util.zip GZip
	 * Implementation does not expose the GZip header so it is difficult to determine
	 * if a string is compressed.
	 * 
	 * @param bytes an array of bytes
	 * @return true if the array is compressed or false otherwise
	 * @throws java.io.IOException if the byte array couldn't be read
	 */
	 public boolean isCompressed(byte[] bytes) throws IOException
	 {
	      if ((bytes == null) || (bytes.length < 2))
	      {
	           return false;
	      }
	      else
	      {
	    	  return ((bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8)));
	      }
	 }
}
