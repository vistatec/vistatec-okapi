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

package net.sf.okapi.steps.xliffkit.opc;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.exceptions.OkapiException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;

public class OPCPackageUtil {

	public static List<PackagePart> getCoreParts(OPCPackage pack) {
		List<PackagePart> res = new ArrayList<PackagePart>();
		
		for (PackageRelationship rel : pack.getRelationshipsByType(TKitRelationshipTypes.CORE_DOCUMENT))
			try {
				res.add(pack.getPart(PackagingURIHelper.createPartName(rel.getTargetURI())));
			} catch (InvalidFormatException e) {
				throw new OkapiException(e);
			}			
		return res;
	}
	
	public static PackagePart getCorePart(OPCPackage pack) {
		List<PackagePart> res = new ArrayList<PackagePart>();
		
		for (PackageRelationship rel : pack.getRelationshipsByType(TKitRelationshipTypes.CORE_DOCUMENT))
			try {
				res.add(pack.getPart(PackagingURIHelper.createPartName(rel.getTargetURI())));
			} catch (InvalidFormatException e) {
				throw new OkapiException(e);
			}			
		return res.size() > 0 ? res.get(0) : null;
	}

	private static PackagePart getPartByRelationshipType(PackagePart part, String relationshipType) {
		try {
			PackageRelationshipCollection rels = part.getRelationshipsByType(relationshipType);
			if (rels.size() == 0) return null;
			
			OPCPackage pack = part.getPackage();
			return pack.getPart(PackagingURIHelper.createPartName(rels.getRelationship(0).getTargetURI()));
		} catch (InvalidFormatException e) {
			throw new OkapiException(e);
		}
	}
	
	public static PackagePart getSourcePart(PackagePart part) {		
		return getPartByRelationshipType(part, TKitRelationshipTypes.SOURCE);
	}	

	public static PackagePart getResourcesPart(PackagePart part) {		
		return getPartByRelationshipType(part, TKitRelationshipTypes.RESOURCES);
	}
}
