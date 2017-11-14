/*===========================================================================
  Copyright (C) 2015-2017 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.filters.xliff;

import java.nio.charset.CharsetEncoder;
import java.util.Map;

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.filterwriter.ITSContent;
import net.sf.okapi.common.filterwriter.XLIFFContent;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;
import net.sf.okapi.filters.xliff.its.ITSStandoffManager;

/**
 * SDL XLIFF specific xliff writer that updates sdl:seg-defs so that the file can be seen as
 * completed.
 * @see <a href="http://google.com">http://wasaty.pl/blog/2012/05/22/sdlxlif-in-memoq-or-there-and-back-again/</a>
 * @author jimh
 *
 */
public class SdlXliffSkeletonWriter extends XLIFFSkeletonWriter {
//	private final Logger logger = LoggerFactory.getLogger(getClass());
	public static final String SDL_SEG_LOCKED_MARKER = "[@#$SDLSEGLOCKED$#@]";
	public static final String SDL_SEG_CONF_MARKER = "[@#$SDLSEGCONF$#@]";
	public static final String SDL_SEG_ORIGIN_MARKER = "[@#$SDLSEGORIGIN$#@]";
	public static final String PROP_SDL_LOCKED = "sdl_lock";
	public static final String PROP_SDL_CONF = "sdl_conf";
	public static final String PROP_SDL_ORIGIN = "sdl_origin";
	public static final String PROP_ORIG_SDL_SEG_CONF = "orig_sdl_seg_conf";

	public SdlXliffSkeletonWriter(Parameters params) {
		super(params);		
	}

	public SdlXliffSkeletonWriter(Parameters params, XLIFFContent fmt, ITSContent itsCont,
			ITSStandoffManager itsStandoffManager, Map<String, GenericAnnotations> lqiStandoff,
			Map<String, GenericAnnotations> provStandoff, CharsetEncoder chsEnc) {
		super(params, fmt, itsCont, itsStandoffManager, lqiStandoff, provStandoff, chsEnc);
	}

	/**
	 * Process as normal xliff but search and replace sdl patterns 
	 * in skeleton.
	 */
	@Override
	public String processTextUnit(ITextUnit resource) {
		// only update the sdl:seg status if there is a target
		if (!resource.getTargetLocales().isEmpty()) {
			// should only be one target
			LocaleId tl = resource.getTargetLocales().iterator().next();
			// skip if no translation 
			if (resource.hasTarget(tl)) {
				resource.setSkeleton(updateSdlSeg(resource.getTarget(tl), (GenericSkeleton)resource.getSkeleton()));
			}
		}
		return super.processTextUnit(resource);
	}

	private ISkeleton updateSdlSeg(TextContainer target, GenericSkeleton skeleton) {
		// scan for sdl:seg
		for (GenericSkeletonPart p : skeleton.getParts()) {
			// replace locked and origin with original values if no values defined in parameters
			Property l = target.getProperty(PROP_SDL_LOCKED);
			Property c = target.getProperty(PROP_SDL_CONF);
			Property o = target.getProperty(PROP_SDL_ORIGIN);

			if (l == null && c == null && o == null) {
				return skeleton;
			}
			String d = p.getData().toString();
			if (l != null) {
				d = d.replace(SDL_SEG_LOCKED_MARKER, Util.isEmpty(getParams().getSdlSegLockedValue()) ? l.getValue() : getParams().getSdlSegLockedValue());
			}
			
			if (c != null) {
				// If the state property added during parsing was removed or can't be mapped back to a conf value
				if (!target.hasProperty(Property.STATE)
						|| !SdlXliffConfLevel.isValidStateValue(target.getProperty(Property.STATE).getValue())) {

					// Replace marker with original conf value if it exists or the default from the config
					if (target.hasProperty(PROP_ORIG_SDL_SEG_CONF) && target.getProperty(PROP_ORIG_SDL_SEG_CONF).getValue() != null) {
						d = d.replace(SDL_SEG_CONF_MARKER, target.getProperty(PROP_ORIG_SDL_SEG_CONF).getValue());
					} else {
						d = d.replace(SDL_SEG_CONF_MARKER, Util.isEmpty(getParams().getSdlSegConfValue()) ? c.getValue() : getParams().getSdlSegConfValue());
					}

				} else {
					// State value exists and can be mapped back to conf
					SdlXliffConfLevel confLevel = SdlXliffConfLevel.fromStateValue(target.getProperty(Property.STATE).getValue());
					d = d.replace(SDL_SEG_CONF_MARKER, confLevel.getConfValue());
				}
			}
			
			if (o != null) {
				d = d.replace(SDL_SEG_ORIGIN_MARKER, Util.isEmpty(getParams().getSdlSegOriginValue()) ? o.getValue() : getParams().getSdlSegOriginValue());				
			}
			p.setData(d);
		}		
		return skeleton;
	}
}
