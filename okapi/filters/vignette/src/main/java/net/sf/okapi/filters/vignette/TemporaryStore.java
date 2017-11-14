/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.vignette;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.okapi.common.exceptions.OkapiException;

/**
 * Simple temporary binary storage and retrieval class for Vignette block. 
 */
class TemporaryStore {

	private static int MAXBLOCKLEN = 65000;
	
	DataOutputStream dos = null;
	DataInputStream dis = null;

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
		StringBuilder tmp = new StringBuilder();
		int count = dis.readInt();
		for ( int i=0; i<count; i++ ) {
			tmp.append(dis.readUTF());
		}
		return tmp.toString();
	}
	
	public void writeBlock (String contentId,
		String data)
	{
		try {
			dos.writeUTF(contentId);
			//dos.writeUTF(data);
			writeLongString(data);
		}
		catch ( IOException e ) {
			throw new OkapiException("Error while writing.", e);
		}
	}

	// Return String[0] = sourceId, String[1] = data 
	public String[] readNext () {
		try {
			String[] res = new String[2];
			res[0] = dis.readUTF();
			//res[1] = dis.readUTF();
			res[1] = readLongString();
			return res;
		}
		catch ( EOFException e ) {
			return null;
		}
		catch ( IOException e ) {
			throw new OkapiException("Error while reading.", e);
		}
	}
	
}
