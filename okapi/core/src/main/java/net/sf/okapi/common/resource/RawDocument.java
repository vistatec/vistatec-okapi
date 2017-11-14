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

package net.sf.okapi.common.resource;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
import net.sf.okapi.common.io.FileCachedInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource that carries all the information needed for a filter to open a given document, and also the resource
 * associated with the event RAW_DOCUMENT. Documents are passed through the pipeline either as RawDocument, or a filter
 * events. Specialized steps allows to convert one to the other and conversely. The RawDocument object has one (and only
 * one) of three input objects: a CharSequence, a URI, or an InputStream.
 */
public class RawDocument implements Closeable, IResource, IWithAnnotations {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public static final String UNKOWN_ENCODING = "null";

	private Annotations annotations;
	private String filterConfigId;
	private String id;
	private String encoding = UNKOWN_ENCODING;
	private LocaleId srcLoc;
	private List<LocaleId> trgLocs;
	private FileCachedInputStream createdStream;
	private URI inputURI;
	private CharSequence inputCharSequence;
	private Reader reader;

	// For output methods
	private URI outputURI;
	private File workFile;

	/**
	 * Creates a new RawDocument object with a given CharSequence and a source locale.
	 * 
	 * @param inputCharSequence
	 *            the CharSequence for this RawDocument.
	 * @param sourceLocale
	 *            the source locale for this RawDocument.
	 */
	public RawDocument (CharSequence inputCharSequence,
		LocaleId sourceLocale)
	{
		create(inputCharSequence, sourceLocale, null);
	}

	/**
	 * Creates a new RawDocument object with a given CharSequence, a source locale and a target locale.
	 * 
	 * @param inputCharSequence
	 *            the CharSequence for this RawDocument.
	 * @param sourceLocale
	 *            the source locale for this RawDocument.
	 * @param targetLocale
	 *            the target locale for this RawDocument.
	 */
	public RawDocument (CharSequence inputCharSequence,
		LocaleId sourceLocale,
		LocaleId targetLocale)
	{
		create(inputCharSequence, sourceLocale, targetLocale);
	}

	/**
	 * Creates a new RawDocument object with a given URI, a default encoding and a source locale.
	 * 
	 * @param inputURI
	 *            the URI for this RawDocument.
	 * @param defaultEncoding
	 *            the default encoding for this RawDocument.
	 * @param sourceLocale
	 *            the source locale for this RawDocument.
	 */
	public RawDocument (URI inputURI,
		String defaultEncoding,
		LocaleId sourceLocale)
	{
		create(inputURI, defaultEncoding, sourceLocale, null);
	}

	/**
	 * Creates a new RawDocument object with a given URI, a default encoding, a source locale and a target locale.
	 * 
	 * @param inputURI
	 *            the URI for this RawDocument.
	 * @param defaultEncoding
	 *            the default encoding for this RawDocument.
	 * @param sourceLocale
	 *            the source locale for this RawDocument.
	 * @param targetLocale
	 *            the target locale for this RawDocument.
	 */
	public RawDocument (URI inputURI,
		String defaultEncoding,
		LocaleId sourceLocale,
		LocaleId targetLocale)
	{
		create(inputURI, defaultEncoding, sourceLocale, targetLocale);
	}

	/**
	 * Creates a new RawDocument object with a given InputStream, a default encoding and a source locale.
	 * 
	 * @param inputStream
	 *            the InputStream for this RawDocument.
	 * @param defaultEncoding
	 *            the default encoding for this RawDocument.
	 * @param sourceLocale
	 *            the source locale for this RawDocument.
	 */
	public RawDocument (InputStream inputStream,
		String defaultEncoding,
		LocaleId sourceLocale)
	{
		try {
			create(inputStream, defaultEncoding, sourceLocale, null);
		} catch (IOException e) {
			throw new OkapiIOException("Error creating or reseting Stream in RawDocument", e);
		}
	}

	/**
	 * Creates a new RawDocument object with a given URI, a default encoding, a source locale and a target locale,
	 * and the filter configuration id.
	 * 
	 * @param inputURI
	 *            the URI for this RawDocument.
	 * @param defaultEncoding
	 *            the default encoding for this RawDocument.
	 * @param sourceLocale
	 *            the source locale for this RawDocument.
	 * @param targetLocale
	 *            the target locale for this RawDocument.
	 * @param filterConfigId
	 *            the filter configuration id.
	 */
	public RawDocument (URI inputURI,
		String defaultEncoding,
		LocaleId sourceLocale,
		LocaleId targetLocale,
		String filterConfigId)
	{
		create(inputURI, defaultEncoding, sourceLocale, targetLocale);
		setFilterConfigId(filterConfigId);
	}
	
	/**
	 * Creates a new RawDocument object with a given InputStream, a default encoding and a source locale.
	 * 
	 * @param inputStream
	 *            the InputStream for this RawDocument.
	 * @param defaultEncoding
	 *            the default encoding for this RawDocument.
	 * @param sourceLocale
	 *            the source locale for this RawDocument.
	 * @param targetLocale
	 *            the target locale for this RawDocument.
	 */
	public RawDocument (InputStream inputStream,
		String defaultEncoding,
		LocaleId sourceLocale,
		LocaleId targetLocale)
	{
		try {
			create(inputStream, defaultEncoding, sourceLocale, targetLocale);
		} catch (IOException e) {
			throw new OkapiIOException("Error creating or reseting Stream in RawDocument", e);
		}
	}

	private void create (CharSequence inputCharSequence,
		LocaleId srcLoc,
		LocaleId trgLoc)
	{
		if (inputCharSequence == null) {
			throw new IllegalArgumentException("inputCharSequence cannot be null");
		}
		this.inputCharSequence = inputCharSequence;
		this.encoding = "UTF-16";
		this.srcLoc = srcLoc;
		this.trgLocs = new ArrayList<LocaleId>(1);
		if ( trgLoc != null ) {
			trgLocs.add(trgLoc);
		}
	}

	private void create (URI inputURI,
		String defaultEncoding,
		LocaleId srcLoc,
		LocaleId trgLoc)
	{
		if (inputURI == null) {
			throw new IllegalArgumentException("inputURI cannot be null");
		}
		this.inputURI = inputURI;
		this.encoding = defaultEncoding;
		this.srcLoc = srcLoc;
		this.trgLocs = new ArrayList<LocaleId>(1);
		if ( trgLoc != null ) {
			trgLocs.add(trgLoc);
		}
	}

	private void create (InputStream inputStream,
		String defaultEncoding,
		LocaleId srcLoc,
		LocaleId trgLoc) throws IOException
	{
		if (inputStream == null) {
			throw new IllegalArgumentException("inputStream cannot be null");
		}

		this.createdStream = createResettableStream(inputStream);
		this.encoding = defaultEncoding;
		this.srcLoc = srcLoc;
		this.trgLocs = new ArrayList<LocaleId>(1);
		if ( trgLoc != null ) {
			trgLocs.add(trgLoc);
		}
	}

	/**
	 * Returns a Reader based on the current Stream returned from getStream(). 
	 * <p>
	 * 
	 * @return a Reader
	 */
	public Reader getReader() {
		if (getEncoding() == UNKOWN_ENCODING) {
			throw new OkapiUnsupportedEncodingException("Encoding has not been set");
		}
		try {
			// clean up any previous readers that were created
			if (reader != null) {
				reader.close();
			}
			reader = new InputStreamReader(createStream(), getEncoding());					
		} catch (UnsupportedEncodingException e) {
			throw new OkapiUnsupportedEncodingException(String.format(
					"The encoding '%s' is not supported.", getEncoding()), e);
		} catch (IOException e) {
			throw new OkapiIOException("Error closing Reader in RawDocument", e);
		}
		
		return reader;
	}

	private FileCachedInputStream createStream() throws IOException {
		// try a normal reset first if this is not the first call of getStream(). But only for the case of CharSequence
		// or URI input. We handle InputStream case a little differently below.
		if (createdStream != null) {
			try {
				// should call reset here as callers may have marked the stream
				// FIXME: we shouldn't call reset here - the caller is responsible
				if (!createdStream.isOpen()) {
					createdStream.reopen();
				}
				createdStream.reset();
				return createdStream;
			} catch (IOException e) {
				try {
					createdStream.dispose();
					createdStream = null;
				} catch (IOException e2) {
				}
			}
		}

		// Either this is the first call to getStream or the reset failed in the above if statement. Now create the
		// streams from the original resource if possible.
		if (getInputCharSequence() != null) {
			try {
				// wrap with InspectableFileCachedInputStream so we get the same semantics for all streams
				// buffer size of the new stream will prevent creation of any temp files
				byte[] bytes = inputCharSequence.toString().getBytes(getEncoding());
				createdStream = StreamUtil.createResettableStream(new ByteArrayInputStream(bytes), bytes.length+1);
			} catch (UnsupportedEncodingException e) {
				throw new OkapiUnsupportedEncodingException(String.format(
						"The encoding '%s' is not supported.", getEncoding()), e);
			}
		} else if (getInputURI() != null) {
			URL url = null;
			try {
				url = getInputURI().toURL();
				createdStream = createResettableStream(url.openStream());
			} catch (IllegalArgumentException e) {
				throw new OkapiIOException("Could not open the URI. The URI must be absolute: "
						+ ((url == null) ? "URL is null" : url.toString()), e);
			} catch (MalformedURLException e) {
				throw new OkapiIOException("Could not open the URI. The URI may be malformed: "
						+ ((url == null) ? "URL is null" : url.toString()), e);
			} catch (IOException e) {
				throw new OkapiIOException(
						"Could not open the URL. The URL is OK but the input stream could not be opened.\n"
						+ e.getMessage(), e);
			}
		}
		
		// in the case of an InputStream createdStream is already created on construction
		return createdStream;
	}
	
	private FileCachedInputStream createResettableStream(InputStream is) throws IOException {
		return StreamUtil.createResettableStream(is, FileCachedInputStream.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Returns an InputStream based on the current input. The underlying {@link FileCachedInputStream} is reset
	 * and reopened if needed.
	 * 
	 * @return the InputStream
	 * @throws OkapiIOException if there was any problem creating the steam.
	 */
	public InputStream getStream() {
		try {
			createdStream = createStream();
		} catch (IOException e) {
			throw new OkapiIOException("Error creating or reseting Stream in RawDocument", e);
		}
		return createdStream;
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.resource.IResource#getAnnotation(java.lang.Class)
	 */
	@Override
	public <A extends IAnnotation> A getAnnotation(Class<A> annotationType) {
		if (annotations == null)
			return null;
		return annotationType.cast(annotations.get(annotationType));
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.resource.IResource#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Always throws an exception as there is never a skeleton associated with a RawDocument.
	 * 
	 * @return never returns.
	 * @throws OkapiNotImplementedException this method is not implemented
	 */
	@Override
	public ISkeleton getSkeleton() {
		throw new OkapiNotImplementedException("The RawDocument resource does not have skeketon");
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.resource.IResource#setAnnotation(net.sf.okapi.common .annotation.IAnnotation)
	 */
	@Override
	public void setAnnotation(IAnnotation annotation) {
		if (annotations == null) {
			annotations = new Annotations();
		}
		annotations.set(annotation);
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.resource.IResource#setId(java.lang.String)
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * This method has no effect as there is never a skeleton for a RawDocument.
	 * @param skeleton the skeleton
	 * @throws OkapiNotImplementedException this method is not implemented
	 */
	@Override
	public void setSkeleton(ISkeleton skeleton) {
		throw new OkapiNotImplementedException("RawDcoument has no skeleton");
	}

	/**
	 * Gets the URI object associated with this resource. It may be null if either CharSequence InputStream inputs are
	 * not null.
	 * 
	 * @return the URI object for this resource (may be null).
	 */
	public URI getInputURI() {
		return inputURI;
	}

	/**
	 * Gets the CharSequence associated with this resource. It may be null if either URI or InputStream inputs are not
	 * null.
	 * 
	 * @return the CHarSequence
	 */
	public CharSequence getInputCharSequence() {
		return inputCharSequence;
	}

	/**
	 * Gets the default encoding associated to this resource.
	 * 
	 * @return The default encoding associated to this resource.
	 */
	public String getEncoding () {
		return encoding;
	}

	/**
	 * Gets the source locale associated to this resource.
	 * 
	 * @return the source locale associated to this resource.
	 */
	public LocaleId getSourceLocale () {
		return srcLoc;
	}

	/**
	 * Sets the source locale associated to this document.
	 * @param locId the locale to set.
	 */
	public void setSourceLocale (LocaleId locId) {
		srcLoc = locId;
	}

	/**
	 * Gets the target locale associated to this resource.
	 * <p>If several targets are set, this method returns the first one.
	 * 
	 * @return the sole or first target locale associated to this resource,
	 * or null if no target locale is set.
	 */
	public LocaleId getTargetLocale () {
		if ( trgLocs.isEmpty() ) return null;
		return trgLocs.get(0);
	}
	
	/**
	 * Sets the target locale associated to this document.
	 * <p>This call overrides any existing target locale or list of target locales.
	 * @param locId the locale to set.
	 */
	public void setTargetLocale (LocaleId locId) {
		trgLocs.clear();
		trgLocs.add(locId);
	}

	/**
	 * Gets the list of target locales associated to this resource.
	 * <p>If the target locale was set using a constructor or {@link #setTargetLocale(LocaleId)},
	 * this list return that locale.
	 * 
	 * @return the target locales associated to this resource. Never null.
	 */
	public List<LocaleId> getTargetLocales () {
		return trgLocs;
	}
	
	/**
	 * Sets the list of target locales associated to this document.
	 * <p>If the target locale was set with a constructor or {@link #setTargetLocale(LocaleId)},
	 * this method overrides that locale.
	 * 
	 * @param locIds the locales to set. If the value is null, an empty list will be associated.
	 */
	public void setTargetLocales (List<LocaleId> locIds) {
		if ( locIds != null ) {
			trgLocs = locIds;
		}
		else {
			trgLocs = new ArrayList<LocaleId>(1);
		}
	}

	/**
	 * Set the input encoding. <h3>WARNING:</h3> Any Readers gotten via getReader() are now invalid. You should call
	 * getReader after calling setEncoding. In some cases it may not be possible to create a new Reader. It is best to
	 * set the encoding <b>before</b> any calls to getReader.
	 * 
	 * @param encoding the encoding to use with the reader.
	 */
	public void setEncoding (String encoding) {
		// Cannot reset an encoding on a CharSequence document
		if (inputCharSequence != null) {
			LOGGER.debug(
					"Cannot reset an encoding on a CharSequence input in RawDocument");
			return;
		}

		if (reader != null) {
			LOGGER.warn("Encoding set with an already vreated Reader. Make sure to call getReader to create a new reader"
					+ "with the new encoding.");
		}
		this.encoding = encoding;
	}
	
	public void setEncoding (Charset encoding) {
		setEncoding(encoding.name());
	}

	/**
	 * Sets the identifier of the filter configuration to use with this document.
	 * 
	 * @param filterConfigId
	 *            the filter configuration identifier to set.
	 */
	public void setFilterConfigId(String filterConfigId) {
		this.filterConfigId = filterConfigId;
	}

	/**
	 * Gets the identifier of the filter configuration to use with this document.
	 * 
	 * @return the the filter configuration identifier for this document, or null if none is set.
	 */
	public String getFilterConfigId() {
		return filterConfigId;
	}
	
	/**
	 * Destroy the underlying stream of this RawDocument and delete all temp reosurces.
	 */
	@Override
	public void close() {
		if (reader != null) {
			try {
				reader.close();
				// help free up resources
				reader = null;
			} catch (IOException e) {
				throw new OkapiIOException("Error closing the reader created by RawDocument.", e);
			}
		}

		if (createdStream != null) {
			try {
				createdStream.close();
				createdStream.dispose();
				createdStream = null;
			} catch (IOException e) {
				throw new OkapiIOException("Error closing the stream created by RawDocument.", e);
			}
		}		
	}

	@Override
	public Annotations getAnnotations() {
		return (annotations == null) ? new Annotations() : annotations;
	}
	
	/**
	 * Creates a new output file object based on a given output URI and the URI of the raw document.
	 * <p>If the path of the raw document is the same as the path of the output a temporary file is created,
	 * otherwise the output URI is used directly.
	 * <b>You must call {@link #finalizeOutput()}</b> when all writing is done and both the input file and output file
	 * are closed to make sure the proper output file name is used.
	 * <p>If one or more directories of the output path do not exist, they are created automatically. 
	 * <p>If the input of the raw document is a CharSequence or a Stream, the method assumes it can
	 * use directly the path of the output URI.
	 * @param outputURI the URI of the output file.
	 * @return the output file.
	 * @throws OkapiIOException if an error occurs when creating the work file or its directory.
	 * @see #finalizeOutput()
	 */
	public File createOutputFile (URI outputURI) {
		this.outputURI = outputURI;
		if ( getInputURI() != null ) {
			String dir = Util.getDirectoryName(outputURI.getPath());
			// If input and output are the same: we need to work with a temporary file
			if ( outputURI.getPath().equals(getInputURI().getPath()) ) {
				try {
					workFile = File.createTempFile("~okapi-11_work_", null, new File(dir));
				}
				catch ( IOException e ) {
					throw new OkapiIOException(String.format("Cannot create temporary file in '%s'.", dir));
				}
				return workFile; // Done
			}
		}
		// Fall back: use the normal output URI
		workFile = new File(outputURI);
		// Make sure the full path exists
		Util.createDirectories(workFile.getAbsolutePath());
		return workFile;
	}
	
	/**
	 * Finalizes the name for this output file.
	 * If a temporary file was used, this call deletes the existing file, 
	 * and then rename the temporary file to the existing file.
	 * This method must always be called after both input and output files are closed.
	 * @throws OkapiIOException if the original input file cannot be deleted or if the work file cannot be renamed. 
	 * @see #createOutputFile(URI)
	 */
	public void finalizeOutput() {
		if ( workFile == null ) return; // Nothing to do
		
		// If the work file is the same as the expected output we are done
		if ( workFile.toURI().equals(outputURI) ) return;
		
		// Otherwise it's a temporary file and we have to rename it
		File outputFile = new File(outputURI);
		if ( outputFile.exists() ) {
			if ( !outputFile.delete() ) {
				// Cannot delete the original input file to replace it with output
				throw new OkapiIOException(String.format("Cannot delete original input file '%s'. The output is still in the temporary file '%s'.",
					outputFile.getAbsolutePath(), workFile.getAbsolutePath()));
			}
		}
		if ( !workFile.renameTo(outputFile) ) {
			// Cannot rename the temporary file
			throw new OkapiIOException(String.format("Cannot rename the temporary output file to '%s'. The output is still under the temporary name '%s'.",
				outputFile.getAbsolutePath(), workFile.getAbsolutePath()));
		}
	}
	
}
