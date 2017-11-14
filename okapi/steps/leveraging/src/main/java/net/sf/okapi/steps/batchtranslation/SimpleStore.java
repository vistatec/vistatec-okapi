/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.batchtranslation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Simple storage and retrieval class for text content (segmented or not). 
 */
class SimpleStore {

	/**
	 * maximum number of characters per writing block.
	 * This is less than MAXINT because the maximum is really in UTF-8 bytes and
	 * we can have 1 char = several bytes in many cases, so there is soom room built-in.
	 */
	private static int MAXBLOCKLEN = 40000;

	private DataOutputStream dos = null;
	private DataInputStream dis = null;

	public void close () {
		try {
			if ( dis != null ) {
				dis.close();
				dis = null;
			}
			if ( dos != null ) {
				dos.close();
				dos = null;
			}
		}
		catch ( IOException e ) {
			throw new OkapiException("Error closing.", e);
		}
	}
	
	public void create (File file) {
		try {
			close();
			dos = new DataOutputStream(new FileOutputStream(file));
		}
		catch ( IOException e ) {
			throw new OkapiException("Error creating.", e);
		}
	}
	
	public void openForRead (File file) {
		try {
			close();
			dis = new DataInputStream(new FileInputStream(file));
		}
		catch ( IOException e ) {
			throw new OkapiException("Error opening.", e);
		}
	}
	
	/**
	 * Writes a text fragment.
	 * @param tf the text fragment to write out.
	 */
	public void write (TextFragment tf) {
		try {
			writeLongString(tf.getCodedText());
			writeLongString(Code.codesToString(tf.getCodes()));
		}
		catch ( IOException e ) {
			throw new OkapiException("Error while writing.", e);
		}
	}

	/**
	 * Reads the next text fragment in the store.
	 * @return the next text fragment in the store, or null if the end is reached.
	 */
	public TextFragment readNext () {
		try {
			String codedText = readLongString();
			String tmp = readLongString();
			TextFragment tf = new TextFragment(codedText, Code.stringToCodes(tmp));
			return tf;
		}
		catch ( EOFException e ) { // Normal end
			return null;
		}
		catch ( IOException e ) {
			throw new OkapiException("Error while reading.", e);
		}
	}
	
	private void writeLongString (String data)
		throws IOException
	{
		int r = (data.length() % MAXBLOCKLEN);
		int n = (data.length() / MAXBLOCKLEN);
		int count = n + ((r > 0) ? 1 : 0);
		
		dos.writeInt(count); // Number of blocks
		int pos = 0;

		// Write the full blocks
		for ( int i=0; i<n; i++ ) {
			dos.writeUTF(data.substring(pos, pos+MAXBLOCKLEN));
			pos += MAXBLOCKLEN;
		}
		// Write the remaining text
		if ( r > 0 ) {
			dos.writeUTF(data.substring(pos));
		}
	}
	
	private String readLongString ()
		throws IOException
	{
		int count = dis.readInt();
		if ( count == 0 ) return "";
		if ( count == 1 ) return dis.readUTF();
		// Else: read the multiple blocks
		StringBuilder tmp = new StringBuilder();
		for ( int i=0; i<count; i++ ) {
			tmp.append(dis.readUTF());
		}
		return tmp.toString();
	}
		
}
