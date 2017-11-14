/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.encoder;

import java.nio.charset.CharsetEncoder;
import java.security.InvalidParameterException;
import java.util.Hashtable;
import java.util.Map.Entry;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.exceptions.OkapiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides caching and lookup mechanism for the text encoders used when writing out text
 * processed by a filter.
 */
public class EncoderManager implements IEncoder {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private Hashtable<String, String> mimeMap; // mimeType to encoder class name map
	private Hashtable<String, IEncoder> encoders; // mimeType to encoder instance map
	private String mimeType = "";
	private IEncoder encoder;
	private String defEncoding;
	private String defLineBreak;
	private IParameters defParams;

	/**
	 * Creates a new encoder manager, with default pre-defined encoder loaded.
	 */
	public EncoderManager () {
		mimeMap = new Hashtable<String, String>();
		encoders = new Hashtable<String, IEncoder>(); 
		// All the filters have their mapping, all mapping should be explicit only
		// Not needed anymore: setAllKnownMappings();
	}

	/**
	 * Sets all mappings known by the core libraries.  
	 */
	public void setAllKnownMappings () {
		// All known mappings
		mimeMap.put(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		mimeMap.put(MimeTypeMapper.XLIFF_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		mimeMap.put(MimeTypeMapper.XLIFF2_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		mimeMap.put(MimeTypeMapper.ODF_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		mimeMap.put(MimeTypeMapper.PROPERTIES_MIME_TYPE, "net.sf.okapi.common.encoder.PropertiesEncoder");
		mimeMap.put(MimeTypeMapper.HTML_MIME_TYPE, "net.sf.okapi.common.encoder.HtmlEncoder");
		mimeMap.put(MimeTypeMapper.XHTML_MIME_TYPE, "net.sf.okapi.common.encoder.HtmlEncoder");
		mimeMap.put(MimeTypeMapper.PO_MIME_TYPE, "net.sf.okapi.common.encoder.POEncoder");
		mimeMap.put(MimeTypeMapper.DOCX_MIME_TYPE, "net.sf.okapi.common.encoder.OpenXMLEncoder");
		mimeMap.put(MimeTypeMapper.DTD_MIME_TYPE, "net.sf.okapi.common.encoder.DTDEncoder");
		mimeMap.put(MimeTypeMapper.TS_MIME_TYPE, "net.sf.okapi.common.encoder.TSEncoder");		
		mimeMap.put(MimeTypeMapper.PHP_MIME_TYPE, "net.sf.okapi.common.encoder.PHPContentEncoder");		
		mimeMap.put(MimeTypeMapper.TTX_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		mimeMap.put(MimeTypeMapper.JSON_MIME_TYPE, "net.sf.okapi.common.encoder.JSONEncoder");
		mimeMap.put(MimeTypeMapper.MARKDOWN_MIME_TYPE, "net.sf.okapi.common.encoder.MarkdownEncoder");
	}

	/**
	 * Clears all encoders from the manager.
	 */
	public void clearMap () {
		mimeMap.clear();
		encoders.clear();
	}
	
	/**
	 * Sets a mapping in the manager. If a mapping for this MIME type exists already
	 * in the manager, it will be overridden by this new one.
	 * @param mimeType The MIME type identifier for this mapping.
	 * @param className The class name of the encoder to use.
	 */
	public void setMapping (String mimeType,
		String className)
	{
		if (this.mimeType.equals(mimeType)) this.mimeType = "";
		
		mimeMap.put(mimeType, className);
		IEncoder encoder = encoders.get(mimeType);
		if (encoder != null && !className.equals(encoder.getClass().getName())) {
			encoders.remove(mimeType);
		}
	}
	
	public void setMapping (String mimeType, IEncoder encoder) {
		if (encoder == null) {
			throw new InvalidParameterException("encoder cannot be null");
		}
		
		if (this.mimeType.equals(mimeType)) this.mimeType = "";
		
		mimeMap.put(mimeType, encoder.getClass().getName());
		encoders.put(mimeType, encoder);
	}
	
	/**
	 * Removes a given mapping from the manager.
	 * @param mimeType The MIME type identifier of the mapping to remove.
	 */
	public void removeMapping (String mimeType) {
		mimeMap.remove(mimeType);
		encoders.remove(mimeType);
	}
	
	/**
	 * Adds the mappings of a given encoder manager into this manager.
	 * If, for a given MIME type, both encoder manager have a different mapping, the original
	 * mapping of this manager remains unchanged and a warning is generated.
	 * @param otherManager the other encoder manager.
	 */
	public void mergeMappings (EncoderManager otherManager) {
		for ( Entry<String, String> entry : otherManager.mimeMap.entrySet() ) {
			// Check if the MIME type is already mapped
			if ( mimeMap.containsKey(entry.getKey()) ) {
				if ( !mimeMap.get(entry.getKey()).equals(entry.getValue()) ) {
					// Same MIME type, but different encoder class name:
					// Generate a warning, and keep the current mapping
					LOGGER.warn(String.format("The MIME type '%s' is currently mapped to '%s', but conflicts with another mapping ('%s').",
						entry.getKey(), mimeMap.get(entry.getKey()), entry.getValue()));
				}
				// Else: Same mapping, nothing to do
			}
			else { // Add the mapping
				setMapping(entry.getKey(), entry.getValue());
			}
		}
		
		for ( Entry<String, IEncoder> entry : otherManager.encoders.entrySet() ) {
			// Check if the MIME type is already mapped
			if ( encoders.containsKey(entry.getKey()) ) {
				if ( !encoders.get(entry.getKey()).equals(entry.getValue()) ) {
					// Same MIME type, but different encoder:
					// Generate a warning, and keep the current mapping
					LOGGER.warn(String.format("The MIME type '%s' is currently mapped to '%s', but conflicts with another mapping ('%s').",
						entry.getKey(), mimeMap.get(entry.getKey().getClass().getName()), entry.getValue().getClass().getName()));
				}
				// Else: Same mapping, nothing to do
			}
			else { // Add the mapping
				setMapping(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	 * Updates the current cached encoder for this manager.
	 * The method {@link #setDefaultOptions(IParameters, String, String)} must have been called
	 * before calling this method.
	 * @param newMimeType The MIME type identifier for the encoder to use now. If there is no mapping for the
	 * given MIME type, the cache is cleared and no encoder is active.
	 */
	public void updateEncoder (String newMimeType) {
		try {
			if ( newMimeType == null ) return;
			// Check if the current encoder is for the same MIME-type
			if ( mimeType.equals(newMimeType) ) return;
		
			// If not: lookup what encoder to use
			mimeType = newMimeType;
			String name = mimeMap.get(mimeType);
			if ( name == null ) { // Not in the map: Use the default one.
				encoder = new DefaultEncoder();
			}
			else { // Else: Instantiate the encoder based on the class name
				if (encoders.containsKey(mimeType)) {
					encoder = encoders.get(mimeType);
				}
				else {
					encoder = (IEncoder)Class.forName(name).newInstance();
					encoders.put(mimeType, encoder);
				}				
			}
			// And set the options
			encoder.setOptions(defParams, defEncoding, defLineBreak);
		}
		catch ( InstantiationException e ) {
			throw new OkapiException(e);
		}
		catch ( IllegalAccessException e ) {
			throw new OkapiException(e);
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiException(e);
		}
	}

	/**
	 * Encodes a given text with the encoder currently cached. If no encoder is currently
	 * cached, the text is returned untouched.
	 * @param text The text to encode.
	 * @param context The context of the text: 0=text, 1=skeleton, 2=inline.
	 * @return The encoded text.
	 */
	@Override
	public String encode (String text,
			EncoderContext context)
	{
		if ( encoder != null ) return encoder.encode(text, context);
		else return text;
	}

	/**
	 * Encodes a given character with the encoder currently cached. If no encoder is currently
	 * cached, the character is returned as its string value.
	 * @param value The character to encode.
	 * @param context The context of the character: 0=text, 1=skeleton, 2=inline.
	 * @return The encoded character 9as a string since it can be now made up of
	 * more than one character).
	 */
	@Override
	public String encode (char value,
			EncoderContext context)
	{
		if ( encoder != null ) return encoder.encode(value, context);
		else return String.valueOf(value); 
	}

	/**
	 * Encodes a given code-point with the encoder currently cached. If no encoder is currently
	 * cached, the character is returned as its string value.
	 * @param codePoint The code-point to encode.
	 * @param context The context of the character: 0=text, 1=skeleton, 2=inline.
	 * @return The encoded character 9as a string since it can be now made up of
	 * more than one character).
	 */
	@Override
	public String encode (int codePoint,
			EncoderContext context)
	{
		if ( encoder != null ) return encoder.encode(codePoint, context);
		else {
			if ( Character.isSupplementaryCodePoint(codePoint) ) {
				return new String(Character.toChars(codePoint));
			}
			return String.valueOf((char)codePoint); 
		}
	}

	/**
	 * Gets the encoder currently cached by this manager.
	 * @return The encoder currently cached by this manager, or null if there is none.
	 */
	public IEncoder getEncoder () {
		return encoder;
	}

	/**
	 * Sets the options for the encoder currently cached. If no encoder is currently
	 * cached, the method does nothing.
	 * @param params The parameters object with all the configuration information 
	 * specific to this encoder.
	 * @param encoding The name of the charset encoding to use.
	 * @param lineBreak Type of line-break to use in the output.
	 */
	@Override
	public void setOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
		if ( encoder != null ) {
			encoder.setOptions(params, encoding, lineBreak);
		}
	}

	@Override
	public String toNative (String propertyName,
		String value)
	{
		if ( encoder != null ) {
			return encoder.toNative(propertyName, value);
		}
		// No change if there is no encoder active
		return value;
	}

	/**
	 * Sets the default options for this encoder manager. The values
	 * passed here are set as the values to use for each encoder as they
	 * are invoked.
	 * @param params The default parameter object.
	 * @param encoding The default encoding.
	 * @param lineBreak The string that the encoder will use as a line break.
	 */
	public void setDefaultOptions (IParameters params,
		String encoding,
		String lineBreak)
	{
		defParams = params;
		defEncoding = encoding;
		if ( lineBreak == null ) {
			throw new InvalidParameterException("lineBreak parameter is null");
		}
		defLineBreak = lineBreak;
	}

	@Override
	public String getLineBreak () {
		if ( encoder != null ) {
			return encoder.getLineBreak();
		}
		return this.defLineBreak;
	}

	@Override
	public CharsetEncoder getCharsetEncoder () {
		if ( encoder != null ) {
			return encoder.getCharsetEncoder();
		}
		return null;
	}

	public final Hashtable<String, String> getMimeMap() {
		return mimeMap;
	}

	public final Hashtable<String, IEncoder> getEncoders() {
		return encoders;
	}

	public final String getMimeType() {
		return mimeType;
	}

	public final String getEncoding() {
		return defEncoding;
	}

	public final String getDefLineBreak() {
		return defLineBreak;
	}

	@Override
	public IParameters getParameters() {
		return defParams;
	}

	/////////////////////////////////////
	// For serialization only
	////////////////////////////////////
	
	protected String getDefEncoding() {
		return defEncoding;
	}

	protected void setDefEncoding(String defEncoding) {
		this.defEncoding = defEncoding;
	}

	protected IParameters getDefParams() {
		return defParams;
	}

	protected void setDefParams(IParameters defParams) {
		this.defParams = defParams;
	}

	protected void setMimeMap(Hashtable<String, String> mimeMap) {
		this.mimeMap = mimeMap;
	}

	protected void setEncoders(Hashtable<String, IEncoder> encoders) {
		this.encoders = encoders;
	}

	protected void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	protected void setEncoder(IEncoder encoder) {
		this.encoder = encoder;
	}

	protected void setDefLineBreak(String defLineBreak) {
		this.defLineBreak = defLineBreak;
	}
}
