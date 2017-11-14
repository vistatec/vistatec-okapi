/*===========================================================================
  Copyright (C) 2011-2014 by the Okapi Framework contributors
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

package net.sf.okapi.lib.transifex;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.Base64;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Basic Transifex client allowing to create and maintain Transifex project from a java application.
 */
public class TransifexClient {

	private static final String HYPHENS = "--";
	private static final String BOUNDARY = "oIkPaApKiO";
	private static final String LINEBREAK = "\r\n"; // HTTP uses CR+LF
	private static final int RESCODE_OK = 200; 
	private static final int RESCODE_CREATED = 201; 
	private static final int MAXBUFFERSIZE = 1024*8; 

	private final SimpleDateFormat dateFormat; 
	private final JSONParser parser;

	private boolean v2;
	private String host;
	private String project;
	private String credentials;
	private String username;

	public TransifexClient (String host) {
		setHost(host);
		parser = new JSONParser();
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");		
	}
	
	public void setHost (String host) {
		// "http://www.transifex.com" for v1 ("api" added automatically)
		// "https://www.transifex.com/api/2" for v2
		this.host = Util.ensureSeparator(host, true);
		v2 = this.host.endsWith("/2/");
	}
	
	public String getHost () {
		return host;
	}
	
	public void setProject (String project) {
		this.project = project;
	}
	
	public String getProject () {
		return project;
	}
	
	public void setCredentials (String username,
		String password)
	{
		this.username = username;
		credentials = "Basic " + Base64.encodeString(username+":"+password);
	}

	/**
	 * Creates a new project if one does not exists already.
	 * If the project exists already it is updated.
	 * This is for the v1 API, use {@link #createProject(String, String, String, LocaleId, boolean, String)} instead.
	 * @param projectId the project Id.
	 * @param name the name of the project.
	 * @param shortDescription a short description (can be null).
	 * @param longDescription a longer description (can be null).
	 * @return an array of strings: On success 0=the project id.
	 * On error 0=null, 1=Error code and message.
	 */
	@Deprecated
	public String[] createProject (String projectId,
		String name,
		String shortDescription,
		String longDescription)
	{
		if ( v2 ) {
			throw new RuntimeException("You must specify a source language when using the V2 API.");
		}
		return createProjectV1(projectId, name, shortDescription, longDescription);
	}
	
	/**
	 * Creates an open-source project new project if one does not exists already.
	 * If the project exists already it is updated.
	 * @param projectId the project Id.
	 * @param name the name of the project.
	 * @param shortDescription a short description (can be null).
	 * @param srcLoc source locale.
	 * @param isPrivate true for a private project, false for a public one.
	 * @param projectURL the URL of the project (mandatory for FOSS projects)
	 * @return an array of strings: On success 0=the project id.
	 * On error 0=null, 1=Error code and message.
	 */
	public String[] createProject (String projectId,
		String name,
		String shortDescription,
		LocaleId srcLoc,
		boolean isPrivate,
		String projectURL)
	{
		if ( shortDescription == null ) {
			// Description cannot be null since v2
			shortDescription = name;
		}
		if ( v2 ) {
			return createProjectV2(projectId, name, shortDescription, null, srcLoc, isPrivate, projectURL);
		}
		else {
			if ( projectURL != null ) {
				throw new RuntimeException("You must use API v2 for FOSS projects.");
			}
			return createProjectV1(projectId, name, shortDescription, "");
		}
	}
	
	private String[] createProjectV1 (String projectId,
		String name,
		String shortDescription,
		String longDescription)
	{
		String[] res = new String[2];
		try {
			URL url = new URL(host + "api/project/"+projectId+"/");
			HttpURLConnection conn = createConnection(url, "POST");
			String data = String.format("{"
				+ "\"slug\": \"%s\", "
				+ "\"name\": \"%s\", "
				+ "\"maintainers\": \"%s\" "
				+ "}",
				projectId, name, username);
			writeData(conn, data);
			
			// Execute
			int code = conn.getResponseCode();
			if ( code == RESCODE_CREATED ) {
				project = projectId;
				res[0] = projectId;
			}
			else {
				res[1] = String.format("Error %d ", code) + conn.getResponseMessage();
			}
		}
		catch ( IOException e ) {
			res[1] = e.getMessage();
		}
		return res;
	}
	
	private String[] createProjectV2 (String projectId,
		String name,
		String shortDescription,
		String longDescription,
		LocaleId srcLoc,
		boolean isPrivate,
		String projectURL)
	{
		String[] res = new String[2];
		try {
			URL url = new URL(host + "projects/");
			HttpURLConnection conn = createConnection(url, "POST");
			String data = String.format("{"
				+ "\"slug\": \"%s\", "
				+ "\"name\": \"%s\", "
				+ "\"description\": \"%s\", "
				+ "\"source_language_code\": \"%s\", "
				+ "\"private\": %s ",
				projectId, name, shortDescription, srcLoc.toPOSIXLocaleId(),
				(isPrivate ? "true" : "false"));
			
			if ( projectURL != null ) {
				data += ", \"repository_url\": \"" + projectURL + "\"";
			}
			data += "}";
			writeData(conn, data);
			
			// Execute
			int code = conn.getResponseCode();
			if ( code == RESCODE_CREATED ) {
				project = projectId;
				res[0] = projectId;
			}
			else {
				res[1] = String.format("Error %d in createProjectV2: ", code) + conn.getResponseMessage();
			}
		}
		catch ( IOException e ) {
			res[1] = e.getMessage();
		}
		return res;
	}
	
	/**
	 * Add a resource to the current project.
	 * If the resource exists already it is updated.
	 * @param poPath the full path of the PO file to add.
	 * @param srcLoc the locale of the source text.
	 * @param resourceFile filename of the resource (must be the same for all languages)
	 * or null to use the filename of the path.
	 * @return An array of strings: On success 0=redirect path, 1=resource Id.
	 * On error: 0=null, 1=Error code and message.
	 */
	public String[] putSourceResource (String poPath,
		LocaleId srcLoc,
		String resourceFile)
	{
		if ( v2 ) {
			return putSourceResourceV2(poPath, srcLoc, resourceFile);
		}
		else { // Else: v1
			String[] res = uploadFileV1(poPath, srcLoc.toPOSIXLocaleId(), resourceFile);
			if ( res[0] == null ) {
				return res; // Could not upload the file
			}
			return extractSourceFromStoredFileV1(res[0], srcLoc.toPOSIXLocaleId());
		}
	}
	
	/**
	 * Creates a new resource and upload the PO file. This is for v2 API.
	 * @param path the path of the PO file.
	 * @param srcLoc the source locale.
	 * @param resourceFile the resource ID to use.
	 * @return An array of strings: On success 0=redirect path, 1=resource Id.
	 * On error: 0=null, 1=Error code and message.
	 */
	private String[] putSourceResourceV2 (String path,
		LocaleId srcLoc,
		String resourceFile)
	{
		String[] res = new String[2];
		String resourceSlug = getSlugFromFile(resourceFile);
		try {
			URL url = new URL(host + "project/"+project+"/resources/");
			HttpURLConnection conn = createConnection(url, "POST");
			String data = String.format("{"
				+ "\"slug\": \"%s\", "
				+ "\"name\": \"%s\", "
				+ "\"i18n_type\": \"PO\",",
				resourceSlug, resourceFile);

			// Read the PO into a string and set it as the content
			// The PO file is expected to have been created by the framework and should be UTF-8
			try ( BufferedReader in = new BufferedReader(
				new InputStreamReader(
					new FileInputStream(path), StandardCharsets.UTF_8)); )
			{
				String line; StringBuilder buf = new StringBuilder();
				while (( line = in.readLine()) != null ) {
					buf.append(escape(line)+"\\n");
				}
				data += ("\"content\": \""+buf.toString()+"\"");
			}
			
			data += "}";
			writeData(conn, data);
			
			// Execute
			int code = conn.getResponseCode();
			if ( code == RESCODE_CREATED ) {
				// Host of V2 ends in "api/2/": remove that
				res[0] = host.substring(0, host.length()-6) + "project/"+project+"/resource/"+resourceSlug;
				res[1] = resourceSlug;
			}
			else {
				res[1] = String.format("Error %d in putSourceResourceV2: ", code) + conn.getResponseMessage();
			}
		}
		catch ( Throwable e ) {
			res[1] = e.getMessage();
		}
		return res;
	}
	
	/**
	 * Escape a given string to JSON escaped text format.
	 * @param text the string to escape.
	 * @return the escaped string.
	 */
	private String escape (String text) {
		String res = text.replace("\\", "\\\\");
		return res.replace("\"", "\\\"");
	}
	
	public String[] putTargetResource (String poPath,
		LocaleId trgLoc,
		String resourceSlug,
		String resourceFile)
	{
		if ( v2 ) {
			return putTargetResourceV2(poPath, trgLoc, resourceSlug, resourceFile);
		}
		else {
			String[] res = uploadFileV1(poPath, trgLoc.toPOSIXLocaleId(), resourceFile);
			if ( res[0] == null ) {
				return res; // Could not upload the file
			}
			return extractTargetFromStoredFileV1(res[0], trgLoc.toPOSIXLocaleId(), resourceSlug);
		}
	}

	public String[] putTargetResourceV2 (String poPath,
		LocaleId trgLoc,
		String resourceSlug,
		String resourceFile)
	{
		String[] res = new String[2];
		try {
			URL url = new URL(host + "project/"+project+"/resource/"+resourceSlug+"/translation/"+trgLoc.toPOSIXLocaleId()+"/");
			HttpURLConnection conn = createConnection(url, "PUT");
			String data = "{";
			// Read the PO into a string and set it as the content
			// The PO file is expected to have been created by the framework and should be UTF-8
			try ( BufferedReader in = new BufferedReader(
				new InputStreamReader(
					new FileInputStream(poPath), StandardCharsets.UTF_8)); )
			{
				String line; StringBuilder buf = new StringBuilder();
				while (( line = in.readLine()) != null ) {
					buf.append(escape(line)+"\\n");
				}
				data += ("\"content\": \""+buf.toString()+"\"");
			}
			
			data += "}";
			writeData(conn, data);
			
			// Execute
			int code = conn.getResponseCode();
			if ( code == RESCODE_OK ) {
				res[0] = url.toString();
				res[1] = resourceSlug;
			}
			else {
				res[1] = String.format("Error %d in putTargetResourceV2: ", code) + conn.getResponseMessage();
			}
		}
		catch ( Throwable e ) {
			res[1] = e.getMessage();
		}
		return res;
	}
	
	/**
	 * Pulls a resource from the current project.
	 * @param resourceId the id of the resource to pull.
	 * @param trgLoc the target locale of the resource to pull.
	 * @param outputPath the output path of the resulting PO file.
	 * @return an array of strings: On success 0=the output path, 1=the resource id.
	 * On error 0=null, 1=the code and error message.
	 */
	public String[] getResource (String resourceId,
		LocaleId trgLoc,
		String outputPath)
	{
		String[] res = null;
		try {
			if ( v2 ) {
				res = retrieveFileV2(resourceId, trgLoc.toPOSIXLocaleId());
			}
			else {
				res = retrieveFileV1(resourceId, trgLoc.toPOSIXLocaleId());
			}
			if ( res[0] == null ) {
				return res;
			}
			// Else: save the PO file
			Util.createDirectories(outputPath);
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputPath), "UTF-8");
			osw.write(res[0]);
			osw.close();
			res[0] = outputPath;
			res[1] = resourceId;
		}
		catch ( IOException e ) {
			res[0] = null;
			res[1] = e.getMessage();
		}
		return res;
	}
	
	/**
	 * Retrieves a file.
	 * @param resId the resource ID of the file to retrieve.
	 * @param lang the language code of the file to retrieve.
	 * @return an array of strings: On success 0=the content of the file, 1=the resource id.
	 * On error 0=null, 1=the code and error messge.
	 */
	private String[] retrieveFileV1 (String resId,
		String lang)
	{
		String[] res = new String[2];
		try {
			URL url = new URL(host + String.format("api/project/%s/resource/%s/%s/file/",
				project, resId, lang));
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestProperty("Authorization", credentials);

			int code = conn.getResponseCode();
			if ( code == RESCODE_OK ) {
				res[0] = readResponse(conn);
				res[1] = resId;
			}
			else {
				res[1] = String.format("Error %d ", code) + conn.getResponseMessage(); 
			}
		}
		catch ( MalformedURLException e ) {
			throw new OkapiIOException("Error retrieving file.", e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error retrieving file.", e);
		}
		
		return res;
	}

	/**
	 * Retrieves a file (version 2)
	 * @param resId the resource ID of the file to retrieve.
	 * @param lang the language code of the file to retrieve.
	 * @return an array of strings: On success 0=the content of the file, 1=the resource id.
	 * On error 0=null, 1=the code and error messge.
	 */
	private String[] retrieveFileV2 (String resId,
		String lang)
	{
		String[] res = new String[2];
		try {
			URL url = new URL(host + String.format("project/%s/resource/%s/translation/%s/",
				project, resId, lang));
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestProperty("Authorization", credentials);

			int code = conn.getResponseCode();
			if ( code == RESCODE_OK ) {
				String str = readResponse(conn);
			    JSONObject object = (JSONObject)parser.parse(str);
				res[0] = (String)object.get("content");
				res[1] = resId;
			}
			else {
				res[1] = String.format("Error %d ", code) + conn.getResponseMessage(); 
			}
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error retrieving file.", e);
		}
		
		return res;
	}

	/**
	 * Extracts a source file from the the storage into the repository.
	 * <p>The file in storage is removed from storage by Transifex.
	 * @param uuid the UUID of the file to extract.
	 * @return String array: On success 0=redirect path to the extracted file, 1=id.
	 * On error: 0=null, 1=Error code and message.
	 */
	private String[] extractSourceFromStoredFileV1 (String uuid,
		String language)
	{
		String[] res = new String[2];
		try {
			URL url = new URL(host + "api/project/" + project + "/files/");
			HttpURLConnection conn = createConnection(url, "POST");
			String data = String.format("{\"uuid\": \"%s\"}", uuid); //, slug=\"%s\"}", uuid, slug));
			writeData(conn, data);

			// {"strings_added": 0, "strings_updated": 0, "redirect": "/projects/p/Icaria/resource/test03pot/"}
			int code = conn.getResponseCode();
			if ( code == RESCODE_OK ) {
			    JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			    res[0] = res[1] = (String)object.get("redirect");
			    if ( res[1].endsWith("/") ) res[1] = res[1].substring(0, res[1].length()-1);
			    res[1] = Util.getFilename(res[1], false);
			}
			else {
				res[1] = String.format("Error %d ", code) + conn.getResponseMessage();
			}
		}
		catch ( MalformedURLException e ) {
			res[1] = e.getMessage();
		}
		catch ( IOException e ) {
			res[1] = e.getMessage();
		}
		catch ( ParseException e ) {
			res[1] = e.getMessage();
		}
		
		return res;
	}

	private String[] extractTargetFromStoredFileV1 (String uuid,
		String language,
		String resourceId)
	{
		String[] res = new String[2];
		try {
			// %(hostname)s/api/project/%(project)s/resource/%(resource)s/%(language)s/
			URL url = new URL(host + "api/project/" + project + "/resource/" + resourceId + "/" + language);
			HttpURLConnection conn = createConnection(url, "PUT");
			
			String data = String.format("{\"uuid\": \"%s\"}", uuid);
			writeData(conn, data);

			int code = conn.getResponseCode();
			if ( code == RESCODE_OK ) {
			    JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			    res[0] = res[1] = (String)object.get("redirect");
			    if ( res[1].endsWith("/") ) res[1] = res[1].substring(0, res[1].length()-1);
			    res[1] = Util.getFilename(res[1], false);
			}
			else {
				res[1] = String.format("Error %d ", code) + conn.getResponseMessage();
			}
		}
		catch ( MalformedURLException e ) {
			res[1] = e.getMessage();
		}
		catch ( IOException e ) {
			res[1] = e.getMessage();
		}
		catch ( ParseException e ) {
			res[1] = e.getMessage();
		}
		
		return res;
	}

	/**
	 * Gets information about a given resource for a given locale.
	 * @param resId the identifier of the resource.
	 * @param locId the locale identifier
	 * @return an array of two objects: On success 0=date 1=completion,
	 * On error: 0=null, 1=null.
	 */
	public Object[] getInformation (String resId,
		LocaleId locId)
	{
		Object res[] = new Object[2];
		try {
			URL url = new URL(host + String.format("api/project/%s/resource/%s/stats/%s/",
				project, resId, locId.toPOSIXLocaleId()));
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestProperty("Authorization", credentials);

			int code = conn.getResponseCode();
			if ( code == RESCODE_OK ) {
				//{ "en": {
				//        "completed": "100%", 
				//        "translated_entities": 10, 
				//        "last_update": "2011-03-03 11:59:09"
				// } }
				JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			    object = (JSONObject)object.get(locId.toPOSIXLocaleId());
			    if ( object == null ) {
			    	res[0] = new Date(0);
			    	res[1] = "0%";
			    }
			    else {
			    	// Get the last update date/time
			    	// (string is in UTC, but not marked as such so we add the time-zone
			    	res[0] = dateFormat.parse((String)object.get("last_update") + " -0000");
			    	// Get the percentage completed
			    	res[1] = (String)object.get("completed");
			    }
			}
			else {
				res[1] = String.format("Error %d ", code) + conn.getResponseMessage(); 
			}
		}
		catch ( MalformedURLException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		catch ( ParseException e ) {
			throw new OkapiIOException("Error parsing results.", e);
		}
		catch ( java.text.ParseException e ) {
			throw new OkapiIOException("Error parsing last update date/time.", e);
		}
		return res;
	}

	/**
	 * Retrieves the list of the resources for the current project, for a given source locale.
	 * @param srcLoc the source locale.
	 * @return an array of object. On success: 0=project id, 1=project short description,
	 * 2=a map of the resource (id and name). On error: 0=null, 1=Error message. 
	 */
	public Object[] getResourceList (LocaleId srcLoc) {
		if ( v2 ) return getResourceListV2(srcLoc);
		else return getResourceListV1(srcLoc);
	}
	
	private Object[] getResourceListV1 (LocaleId srcLoc) {
		Object[] res = new Object[3];
		res[0] = null;
		try {
			URL url = new URL(host + String.format("api/project/%s/", project));
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestProperty("Authorization", credentials);

			int code = conn.getResponseCode();
			String srcLang = srcLoc.toPOSIXLocaleId();
			if ( code == RESCODE_OK ) {
				Map<String, ResourceInfo> resources = new HashMap<String, ResourceInfo>();
				res[2] = resources;
				// See http://help.transifex.net/technical/api/api.html
				JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				res[0] = (String)object.get("slug");
				res[1] = (String)object.get("description");
				JSONArray array = (JSONArray)object.get("resources");
				for ( int i=0; i<array.size(); i++ ) {
					object = (JSONObject)array.get(i);
					JSONObject object2 = (JSONObject)object.get("source_language");
					String lang = (String)object2.get("code");
					if ( !srcLang.equals(lang) ) continue;
					// Else: This is a resource for the given source locale
					String i18nType = (String)object.get("i18n_type");
					String resId = (String)object.get("slug");
					String name = (String)object.get("name");
					resources.put(resId, new ResourceInfo(resId, name, i18nType, true));
				}
			}
			else {
				res[1] = String.format("Error %d ", code) + conn.getResponseMessage(); 
			}
		}
		catch ( MalformedURLException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		catch ( ParseException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		
		return res;
	}
	
	private Object[] getResourceListV2 (LocaleId srcLoc) {
		Object[] res = new Object[3];
		res[0] = null;
		try {
			URL url = new URL(host + String.format("project/%s/resources", project));
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestProperty("Authorization", credentials);

			int code = conn.getResponseCode();
			String srcLang = srcLoc.toPOSIXLocaleId();
			if ( code == RESCODE_OK ) {
				res[0] = project;
				res[1] = ""; // TODO: Description
				Map<String, ResourceInfo> resources = new HashMap<String, ResourceInfo>();
				JSONArray array = (JSONArray)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				for ( int i=0; i<array.size(); i++ ) {
					JSONObject object = (JSONObject)array.get(i);
					String lang = (String)object.get("source_language_code");
					if ( !srcLang.equals(lang) ) continue;
					// Else: This is a resource for the given source locale
					String i18nType = (String)object.get("i18n_type");
					String resId = (String)object.get("slug");
					String name = (String)object.get("name");
					resources.put(resId, new ResourceInfo(resId, name, i18nType, true));
				}
				res[2] = resources;
			}
			else {
				res[1] = String.format("Error %d ", code) + conn.getResponseMessage(); 
			}
		}
		catch ( MalformedURLException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		catch ( ParseException e ) {
			throw new OkapiIOException("Error retrieving info.", e);
		}
		
		return res;
	}

	/**
	 * Upload a file to the storage.
	 * <p>The file must be a POT file, in UTF-8 without BOM.
	 * @param path the path of the POT file to upload. 
	 * @param resourceFile filename of the resource (must be the same for all languages)
	 * or null to use the filename of the path.
	 * @return an array of strings: On success 0=UUID, 1=Resource filename, 2=Resource id.
	 * On error 0=null, 1=error code and message, 2=null.
	 */
	private String[] uploadFileV1 (String path,
		String language,
		String resourceFile)
	{
		DataOutputStream dos = null;
		String[] res = new String[3];
		try (FileInputStream fis = new FileInputStream(path);) {
			// Set the default value for the resource file if needed
			if ( resourceFile == null ) {
				resourceFile = Util.getFilename(path, true);
			}
			
			URL url = new URL(host + "api/storage/");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
		    conn.setDoInput(true);
		    conn.setAllowUserInteraction(false);
			conn.setRequestProperty("Authorization", credentials);
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);

			dos = new DataOutputStream(conn.getOutputStream());

			addFormDataPart("resource", resourceFile, dos);
			addFormDataPart("language", language, dos);
			
			dos.writeBytes(HYPHENS + BOUNDARY + LINEBREAK);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";"
				+ " filename=\"" + resourceFile + "\"" + LINEBREAK);
			dos.writeBytes("Content-Type: application/octet-stream"
				+ LINEBREAK + LINEBREAK);
			
			int bytesAvailable = fis.available();
			int bufferSize = Math.min(bytesAvailable, MAXBUFFERSIZE);
			byte[] buffer = new byte[bufferSize];

			// Read and write the file
			int bytesRead = fis.read(buffer, 0, bufferSize);
			while ( bytesRead > 0 ) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fis.available();
			    bufferSize = Math.min(bytesAvailable, MAXBUFFERSIZE);
			    bytesRead = fis.read(buffer, 0, bufferSize);
			}

			// Close form data
			dos.writeBytes(LINEBREAK);
			dos.writeBytes(HYPHENS + BOUNDARY + HYPHENS + LINEBREAK);
			dos.flush();
			dos.close();

			int code =  conn.getResponseCode();
			if ( code == RESCODE_OK ) {
				String str = readResponse(conn);
			    JSONObject object = (JSONObject)parser.parse(str);
			    JSONArray files = (JSONArray)object.get("files");
			    if ( files.size() == 1 ) {
			    	JSONObject file = (JSONObject)files.get(0);
			    	res[0] = (String)file.get("uuid");
			    	res[1] = (String)file.get("name");
			    	res[2] = (String)file.get("id");
			    }
			    else {
			    	res[1] = String.format("Success returned, but no file description returned. Response='%s'", str);
			    }
			}
			else {
				res[1] = String.format("Error %d ", code) + conn.getResponseMessage();
			}
		}
		catch ( MalformedURLException e ) {
			res[1] = e.getMessage();
		}
		catch ( IOException e ) {
			res[1] = e.getMessage();
		}
		catch ( ParseException e ) {
			res[1] = e.getMessage();
		}
		return res;
	}

	private HttpURLConnection createConnection (URL url,
		String requestType)
		throws IOException
	{
		HttpURLConnection conn = null;
		conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod(requestType);
		conn.setDoOutput(true);
	    conn.setDoInput(true);
	    conn.setAllowUserInteraction(false);
		conn.setRequestProperty("Authorization", credentials);
		conn.setRequestProperty("Content-Type", "application/json");
		return conn;
	}
	
	private void addFormDataPart (String name,
		String value,
		DataOutputStream dos)
		throws IOException
	{
		dos.writeBytes(HYPHENS + BOUNDARY + LINEBREAK);
		dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\""
			+ LINEBREAK + LINEBREAK);
		dos.writeBytes(value + LINEBREAK);
	}

	private String readResponse (HttpURLConnection conn)
		throws UnsupportedEncodingException, IOException
	{
		StringBuilder tmp = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
				new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ( (line = reader.readLine()) != null ) {
				tmp.append(line+"\n");
			}
		}
		finally {
			if ( reader != null ) {
				reader.close();
			}
		}
		return tmp.toString();
	}

	private String getSlugFromFile (String resourceFile) {
		return resourceFile.replace('.', '_');
	}
	
	private void writeData (HttpURLConnection conn,
		String data)
		throws IOException
	{
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(data);
			dos.flush();
		}
		finally {
			dos.close();
		}
	}
	
}
