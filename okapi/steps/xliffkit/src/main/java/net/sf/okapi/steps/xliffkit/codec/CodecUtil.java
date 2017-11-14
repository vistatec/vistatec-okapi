/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffkit.codec;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;

public class CodecUtil {

	private enum Operation {
		ENCODE,
		DECODE
	}
	
	private static void processTC(TextContainer tc, Operation op, ICodec codec) {
		for (TextPart textPart : tc) {
			if (textPart.isSegment()) {
				
				// Process coded text
				TextFragment tf = textPart.getContent();
				String codedText = tf.getCodedText();
				switch (op) {
				case ENCODE:
					codedText = codec.encode(codedText);					
					break;

				case DECODE:
					codedText = codec.decode(codedText);
					break;
					
				default:
					break;
				}
				tf.setCodedText(codedText);
				
				// Process code data
				for (Code code : tf.getCodes()) {
					if (code.hasOuterData()) {
						code.setOuterData(code.getOuterData());
					}
					
					if (code.hasData()) {
						code.setData(code.getData());
					}
				}
			}
		}
	}
	
	private static void processTU(ITextUnit tu, Operation op, ICodec codec) {
		processTC(tu.getSource(), op, codec);
		for (LocaleId trgLoc : tu.getTargetLocales()) {			
			processTC(tu.getTarget(trgLoc), op, codec);
		}
	}
	
	public static void encodeTextUnit(ITextUnit tu, ICodec codec) {
		processTU(tu, Operation.ENCODE, codec);
	}
	
	public static void decodeTextUnit(ITextUnit tu, ICodec codec) {
		processTU(tu, Operation.DECODE, codec);
	}
}
