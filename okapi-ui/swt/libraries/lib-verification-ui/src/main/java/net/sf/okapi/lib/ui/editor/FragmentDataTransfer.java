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

package net.sf.okapi.lib.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.sf.okapi.common.resource.Code;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * Helper class to handle clipboard transfer of extracted text.
 */
public class FragmentDataTransfer extends ByteArrayTransfer {

	static private final String MIME_TYPE = "application/okapi-fragment"; // $NON-NLS-1$

	static private FragmentDataTransfer theInstance;
	
	private final int MIME_TYPE_ID = registerType(MIME_TYPE);

	private FragmentDataTransfer () {
		// Nothing to do
	}
	
	public static FragmentDataTransfer getInstance () {
		if ( theInstance == null ) {
			theInstance = new FragmentDataTransfer();
		}
		return theInstance;
	}
	
	@Override
	protected int[] getTypeIds () {
		return new int[] {MIME_TYPE_ID};
	}

	@Override
	protected String[] getTypeNames () {
		return new String[] {MIME_TYPE};
	}

	@Override
	public void javaToNative(Object object,
		TransferData transferData)
	{
		if ( !isSupportedType(transferData) ) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		FragmentData myType = (FragmentData)object;
		byte[] bytes = convertToByteArray(myType);
		if ( bytes != null ) {
			super.javaToNative(bytes, transferData);
		}
	}

	@Override
	public Object nativeToJava (TransferData transferData) {
		if ( !isSupportedType(transferData) ) return null;
		byte[] bytes = (byte[])super.nativeToJava(transferData);
		return ((bytes == null) ? null : restoreFromByteArray(bytes));
	}

	private static byte[] convertToByteArray (FragmentData data) {
		DataOutputStream dos = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			dos = new DataOutputStream(bos);
			dos.writeUTF(data.codedText);
			dos.writeUTF(Code.codesToString(data.codes));
			return bos.toByteArray();
		}
		catch ( IOException e ) {
			return null;
		}
		finally {
			if ( dos != null ) {
				try {
					dos.close();
				}
				catch ( IOException e ) {}
			}
		}
	}
	
	private static FragmentData restoreFromByteArray (byte[] bytes) {
		DataInputStream dis = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			dis = new DataInputStream(bis);
			FragmentData result = new FragmentData();
			result.codedText = dis.readUTF();
			result.codes = Code.stringToCodes(dis.readUTF());
			return result;
		}
		catch ( IOException ex ) {
			return null;
		}
		finally {
			if ( dis != null ) {
				try {
					dis.close();
				}
				catch ( IOException e ) {} // Swallow the error
			}
		}
	}

}
