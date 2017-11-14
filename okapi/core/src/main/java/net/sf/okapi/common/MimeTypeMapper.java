/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Provides definitions for common MIME types and mappings from file extensions
 * to MIME type.
 */
public final class MimeTypeMapper {
	private static final Hashtable<String, String> extensionToMimeMap = new Hashtable<String, String>();

	public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
	public static final String XML_MIME_TYPE = "text/xml";
	public static final String ODF_MIME_TYPE = "text/x-odf";
	public static final String HTML_MIME_TYPE = "text/html";
	public static final String XHTML_MIME_TYPE = "text/xhtml";
	public static final String PO_MIME_TYPE = "application/x-gettext";
	public static final String XLIFF_MIME_TYPE = "application/x-xliff+xml";
	public static final String XLIFF2_MIME_TYPE = "application/xliff+xml"; // http://docs.oasis-open.org/xliff/xliff-media/v2.0/xliff-media-v2.0.html
	public static final String RTF_MIME_TYPE = "application/rtf";
	public static final String MS_DOC_MIME_TYPE = "application/msword";
	public static final String MS_EXCEL_MIME_TYPE = "application/vnd.ms-excel";
	public static final String MS_POWERPOINT_MIME_TYPE = "application/vnd.ms-powerpoint";
	public static final String JAVASCRIPT_MIME_TYPE = "application/x-javascript";
	public static final String CSV_MIME_TYPE = "text/csv";
	public static final String INDESIGN_MIME_TYPE = "text/inx";
	public static final String MIF_MIME_TYPE = "application/vnd.mif";
	public static final String PLAIN_TEXT_MIME_TYPE = "text/plain";
	public static final String QUARK_MIME_TYPE = "text/qml";
	public static final String FLASH_MIME_TYPE = "text/x-flash-xml";
	public static final String PROPERTIES_MIME_TYPE = "text/x-properties";
	public static final String DTD_MIME_TYPE = "application/xml+dtd";
	public static final String SERVER_SIDE_INCLUDE_MIME_TYPE = "text/x-ssi";
	public static final String DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String DOCM_MIME_TYPE = "application/vnd.ms-word.document.macroenabled.12";
	public static final String DOTX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
	public static final String DOTM_MIME_TYPE = "application/vnd.ms-word.template.macroenabled.12";
	public static final String PPTX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
	public static final String PPTM_MIME_TYPE = "application/vnd.ms-powerpoint.presentation.macroenabled.12";
	public static final String PPSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
	public static final String PPSM_MIME_TYPE = "application/vnd.ms-powerpoint.slideshow.macroenabled.12";
	public static final String POTX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.template";
	public static final String POTM_MIME_TYPE = "application/vnd.ms-powerpoint.template.macroenabled.12";
	public static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String XLSM_MIME_TYPE = "application/vnd.ms-excel.sheet.macroenabled.12";
	public static final String XLTX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
	public static final String XLTM_MIME_TYPE = "application/vnd.ms-excel.template.macroenabled.12";
	public static final String VSDX_MIME_TYPE = "application/vnd.visio2013";
	public static final String VSDM_MIME_TYPE = VSDX_MIME_TYPE;
	public static final String TS_MIME_TYPE = "application/x-ts";	
	public static final String PHP_MIME_TYPE = "application/x-php";
	public static final String OPENOFFICE_MIME_TYPE = "application/x-openoffice";
	public static final String TTX_MIME_TYPE = "application/x-ttx+xml";
	public static final String TMX_MIME_TYPE = "application/x-tmx+xml";
	public static final String VERSIFIED_TXT_MIME_TYPE = "text/x-versified-txt";
	public static final String XINI_MIME_TYPE = "text/x-xini";
	public static final String DOXYGEN_TXT_MIME_TYPE = "text/x-doxygen-txt";
	public static final String TRANSIFEX = "application/x-transifex";
	public static final String JSON_MIME_TYPE = "application/json";
	public static final String TXML_MIME_TYPE = "application/x-txml";
	public static final String ICML_MIME_TYPE = "application/x-icml+xml";
	public static final String YAML_MIME_TYPE = "text/x-yaml";
	public static final String PDF_MIME_TYPE = "application/pdf";
	public static final String MARKDOWN_MIME_TYPE = "text/x-markdown";

	static {
		extensionToMimeMap.put("xml", XML_MIME_TYPE);
		extensionToMimeMap.put("odf", ODF_MIME_TYPE);
		extensionToMimeMap.put("html", HTML_MIME_TYPE);
		extensionToMimeMap.put("htm", HTML_MIME_TYPE);
		extensionToMimeMap.put("xhtml", XHTML_MIME_TYPE);
		extensionToMimeMap.put("po", PO_MIME_TYPE);
		extensionToMimeMap.put("rtf", RTF_MIME_TYPE);
		extensionToMimeMap.put("doc", MS_DOC_MIME_TYPE);
		extensionToMimeMap.put("xls", MS_EXCEL_MIME_TYPE);
		extensionToMimeMap.put("ppt", MS_POWERPOINT_MIME_TYPE);
		extensionToMimeMap.put("js", JAVASCRIPT_MIME_TYPE);
		extensionToMimeMap.put("csv", CSV_MIME_TYPE);
		extensionToMimeMap.put("inx", INDESIGN_MIME_TYPE);
		extensionToMimeMap.put("mif", MIF_MIME_TYPE);
		extensionToMimeMap.put("txt", PLAIN_TEXT_MIME_TYPE);
		extensionToMimeMap.put("qml", QUARK_MIME_TYPE);
		extensionToMimeMap.put("flash", FLASH_MIME_TYPE);
		extensionToMimeMap.put("properties", PROPERTIES_MIME_TYPE);
		extensionToMimeMap.put("ssi", SERVER_SIDE_INCLUDE_MIME_TYPE);
		extensionToMimeMap.put("docx", DOCX_MIME_TYPE);
		extensionToMimeMap.put("docm", DOCM_MIME_TYPE);
		extensionToMimeMap.put("dotx", DOTX_MIME_TYPE);
		extensionToMimeMap.put("dotm", DOTM_MIME_TYPE);
		extensionToMimeMap.put("pptx", PPTX_MIME_TYPE);
		extensionToMimeMap.put("pptm", PPTM_MIME_TYPE);
		extensionToMimeMap.put("ppsx", PPSX_MIME_TYPE);
		extensionToMimeMap.put("ppsm", PPSM_MIME_TYPE);
		extensionToMimeMap.put("potx", POTX_MIME_TYPE);
		extensionToMimeMap.put("potm", POTM_MIME_TYPE);
		extensionToMimeMap.put("xlsx", XLSX_MIME_TYPE);
		extensionToMimeMap.put("xlsm", XLSM_MIME_TYPE);
		extensionToMimeMap.put("xltx", XLTX_MIME_TYPE);
		extensionToMimeMap.put("xltm", XLTM_MIME_TYPE);
		extensionToMimeMap.put("vsdx", VSDX_MIME_TYPE);
		extensionToMimeMap.put("vsdm", VSDM_MIME_TYPE);
		extensionToMimeMap.put("dtd", DTD_MIME_TYPE);
		extensionToMimeMap.put("ts", TS_MIME_TYPE);		
		extensionToMimeMap.put("odt", OPENOFFICE_MIME_TYPE);		
		extensionToMimeMap.put("ods", OPENOFFICE_MIME_TYPE);		
		extensionToMimeMap.put("odp", OPENOFFICE_MIME_TYPE);		
		extensionToMimeMap.put("odg", OPENOFFICE_MIME_TYPE);		
		extensionToMimeMap.put("vrsz", VERSIFIED_TXT_MIME_TYPE);	
		extensionToMimeMap.put("xini", XINI_MIME_TYPE);	
		extensionToMimeMap.put("h", DOXYGEN_TXT_MIME_TYPE);
		extensionToMimeMap.put("c", DOXYGEN_TXT_MIME_TYPE);
		extensionToMimeMap.put("cpp", DOXYGEN_TXT_MIME_TYPE);
		extensionToMimeMap.put("java", DOXYGEN_TXT_MIME_TYPE);
		extensionToMimeMap.put("py", DOXYGEN_TXT_MIME_TYPE);
		extensionToMimeMap.put("pdf", PDF_MIME_TYPE);
		extensionToMimeMap.put("m", DOXYGEN_TXT_MIME_TYPE);
		extensionToMimeMap.put("xlf", XLIFF_MIME_TYPE);
		extensionToMimeMap.put("json", JSON_MIME_TYPE);
		extensionToMimeMap.put("tmx", TMX_MIME_TYPE);
		extensionToMimeMap.put("icml", ICML_MIME_TYPE);
		extensionToMimeMap.put("wcml", ICML_MIME_TYPE);
		extensionToMimeMap.put("yml", YAML_MIME_TYPE);
		extensionToMimeMap.put("yaml", YAML_MIME_TYPE);

	}

	/**
	 * Gets the mime type associated with the provided file extension. Some mime
	 * types map to many file extensions. Some file extensions map to many mime
	 * types. For example, there are many types of xml files which have
	 * different mime types.
	 * 
	 * @param extension
	 *            the file extension to lookup (without the dot prefix)
	 * @return the mime type (UNKOWN_MIME_TYPE if the extension is not known).
	 */
	public static String getMimeType(String extension) {
		String mimeType = extensionToMimeMap.get(extension);
		if ( mimeType == null ) {
			return DEFAULT_MIME_TYPE;
		}
		return mimeType;
	}

	/**
	 * Returns true if Okapi supports segmentation for the format denoted by a given mimeType.
	 * Segmentation support for Okapi means the filter for a given format inserts segment source and
	 * target placeholders in the resources created by the filter. 
	 * @param mimeType the given mime type.
	 * @return true if segmentation is supported for the given format.
	 */	
	public static boolean isSegmentationSupported(String mimeType) {
		final List<String> SEG_FORMATS = Arrays.asList(
				XLIFF_MIME_TYPE,
				XLIFF2_MIME_TYPE,
				TMX_MIME_TYPE,
				TTX_MIME_TYPE,
				TXML_MIME_TYPE,
				XINI_MIME_TYPE
				);
		// TODO Should we tell segmentation support by a filter configuration too/instead?
		return SEG_FORMATS.contains(mimeType);				
	}
}
