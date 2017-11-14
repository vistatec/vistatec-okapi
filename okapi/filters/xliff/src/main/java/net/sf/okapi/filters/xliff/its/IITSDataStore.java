/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xliff.its;

import java.util.Collection;

/**
 * Methods needed to properly implement the backend for storing ITS standoff
 * references.
 */
public interface IITSDataStore {

	/**
	 * Setup IITSDataStore with an identifier marking each time a new file
	 * is opened for parsing.
	 */
	public void initialize(String identifier);

	/**
	 * Return a list of stored LQI metadata URIs.
	 */
	public Collection<String> getStoredLQIURIs();

	/**
	 * Return a list of stored Provenance metadata URIs.
	 */
	public Collection<String> getStoredProvURIs();

	/**
	 * Fetch LQI metadata by URI.
	 */
	public ITSLQICollection getLQIByURI(String uri);

	/**
	 * Fetch Provenance metadata by URI.
	 */
	public ITSProvenanceCollection getProvByURI(String uri);

	/**
	 * Store LQI metadata.
	 */
	public void save(ITSLQICollection lqi);

	/**
	 * Store Provenance metadata.
	 */
	public void save(ITSProvenanceCollection prov);
}
