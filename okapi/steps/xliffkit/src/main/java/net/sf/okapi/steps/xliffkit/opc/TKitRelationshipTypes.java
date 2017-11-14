/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffkit.opc;


public interface TKitRelationshipTypes {

	/**
	 * Core document relationship type.
	 */
	String CORE_DOCUMENT = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";

	/**
	 * Skeleton relationship type.
	 */
	String SKELETON = "http://schemas.okapi.org/2010/relationships/skeleton";
	
	/**
	 * Resources relationship type.
	 */
	String RESOURCES = "http://schemas.okapi.org/2010/relationships/resources";
	
	/**
	 * Source document relationship type.
	 */
	String SOURCE = "http://schemas.okapi.org/2010/relationships/source";
	
	/**
	 * Document original relationship type.
	 */
	String ORIGINAL = "http://schemas.okapi.org/2010/relationships/original";
	
	/**
	 * Core properties relationship type.
	 */
	String CORE_PROPERTIES = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";

	/**
	 * Extended properties relationship type.
	 */
	String EXTENDED_PROPERTIES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties";
	
	/**
	 * Custom properties relationship type.
	 */
	String CUSTOM_PROPERTIES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties";
	
	/**
	 * Custom XML relationship type.
	 */
	String CUSTOM_XML = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml";

	/**
	 * Thumbnail relationship type.
	 */
	String THUMBNAIL = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail";
	
	/**
	 * Image part relationship type.
	 */
	String IMAGE_PART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";

	/**
	 * Style part relationship type.
	 */
	String STYLE_PART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";
	
	/**
	 * Audio part relationship type. 
	 */
	String AUDIO_PART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/audio";
	
	/**
	 * Video part relationship type.
	 */
	String VIDEO_PART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/video";
	
	/**
	 * Embedded package relationship type.
	 */
	String EMBEDDED_PACKAGE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/package";
	
	/**
	 * Embedded font relationship type.
	 */
	String EMBEDDED_FONT = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/font";
	
	/**
	 * Digital signature relationship type.
	 */
	String DIGITAL_SIGNATURE = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/signature";

	/**
	 * Digital signature certificate relationship type.
	 */
	String DIGITAL_SIGNATURE_CERTIFICATE = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/certificate";

	/**
	 * Digital signature origin relationship type.
	 */
	String DIGITAL_SIGNATURE_ORIGIN = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/origin";
	
	
}
