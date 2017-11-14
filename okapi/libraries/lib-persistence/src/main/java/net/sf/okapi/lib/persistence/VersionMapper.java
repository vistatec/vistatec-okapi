/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;

public class VersionMapper {

	private static Map<String, IVersionDriver> versionMap = new ConcurrentHashMap<String, IVersionDriver>();
	private static Map<String, String> versionIdMap = new ConcurrentHashMap<String, String>(); 
	
	public static void registerVersion(Class<? extends IVersionDriver> versionDriverClass) {
		IVersionDriver versionDriver = null;
		try {
			versionDriver = ClassUtil.instantiateClass(versionDriverClass);
		} catch (InstantiationException e) {
			throw(new OkapiException(String.format("VersionMapper: cannot instantiate version driver %s", 
					ClassUtil.getQualifiedClassName(versionDriverClass))));
		} catch (IllegalAccessException e) {
			throw(new OkapiException(String.format("VersionMapper: cannot instantiate version driver %s", 
					ClassUtil.getQualifiedClassName(versionDriverClass))));
		}
		versionMap.put(versionDriver.getVersionId(), versionDriver);
	}
	
	public static IVersionDriver getDriver(String versionId) {
		return versionMap.get(getMapping(versionId));
	}
	
	/**
	 * Maps a VersionId to another VersionId. Used for backwards compatibility with older formats if the VersionId has changed.   
	 * @param previousVersionId old VersionId
	 * @param versionId new VersionId
	 */
	public static void mapVersionId(String previousVersionId, String versionId) {
		versionIdMap.put(previousVersionId, versionId);
	}
	
	public static String getMapping(String versionId) {
		String vid = versionIdMap.get(versionId); 
		return Util.isEmpty(vid) ? versionId : vid; // if no mapping is set, return the original version Id
	}
}
