/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.tm.pensieve.common;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;

/**
 * A helper class
 */
public final class PensieveUtil {

    private PensieveUtil(){}

    /**
     * Converts an un-segmented TextUnit into a TranslationUnit. The target may not exist, in that case
     * its content is stored as a null.
     * @param sourceLoc the source locale to transform.
     * @param targetLoc the target locale to transform.
     * @param textUnit the text unit to convert.
     * @return a TranslationUnit that represents the TextUnit
     */
    public static TranslationUnit convertToTranslationUnit (LocaleId sourceLoc,
    	LocaleId targetLoc,
    	ITextUnit textUnit)
    {
        TranslationUnitVariant source = new TranslationUnitVariant(sourceLoc, textUnit.getSource().getFirstContent());
        TextContainer tc = textUnit.getTarget(targetLoc); // Allow null target content
        TranslationUnitVariant target = new TranslationUnitVariant(targetLoc, (( tc==null ) ? null : tc.getFirstContent()));
        TranslationUnit tu = new TranslationUnit(source, target);
        populateMetaDataFromProperties(textUnit, tu);
        return tu;
    }

    /**
     * Converts a TranslationUnit to a TextUnit
     * @param tu The TranslationUnit to convert.
     * @return The converted TextUnit
     */
    public static ITextUnit convertToTextUnit (TranslationUnit tu) {
        ITextUnit textUnit;
        String tuid = tu.getMetadata().get(MetadataType.ID);

        textUnit = new TextUnit(tuid);
        if (tuid != null) {
            textUnit.setName(tuid);
        }
        textUnit.setSourceContent(tu.getSource().getContent());
        textUnit.setTargetContent(tu.getTarget().getLanguage(), tu.getTarget().getContent());
        for (MetadataType type : tu.getMetadata().keySet()) {
            if (type != MetadataType.ID) {
                textUnit.setProperty(new Property(type.fieldName(), tu.getMetadata().get(type)));
            }
        }
        return textUnit;
    }

    private static void populateMetaDataFromProperties (ITextUnit textUnit,
    	TranslationUnit tu)
    {
        MetadataType mdt;
        for (String key : textUnit.getPropertyNames()) {
           mdt = MetadataType.findMetadataType(key);
            if (mdt != null) {
                tu.getMetadata().put(mdt, textUnit.getProperty(key).getValue());
            } else {
               //TODO: What do we do if mapping for metadata doesn't exist?  It is enough for simpleTM replacement to
                //support a finite set.
            }
        }
    }
}
