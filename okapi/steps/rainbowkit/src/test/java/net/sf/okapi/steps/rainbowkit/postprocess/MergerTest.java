/*===========================================================================
"#, fuzzy\r""#, fuzzy\r"  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.postprocess;

import static org.junit.Assert.assertEquals;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnitUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MergerTest {
	
	private GenericContent fmt = new GenericContent();

	@Test
	public void testTransfer () {
		// source: <i>Before</i> <b>bold<i> after.
		// target: <b>XXXXXX</b> <i>XXXX</i> XXXX.
		TextFragment tf = new TextFragment("s1");
		tf.append(TagType.OPENING, "i", "<i>", 1);
		tf.append("s2");
		tf.append(TagType.CLOSING, "i", "</i>", 1);
		tf.append(" ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("s3");
		tf.append(TagType.OPENING, "i", "<i>");
		tf.append("s3");
		assertEquals("s1<i>s2</i> <b>s3<i>s3", tf.toText());
		assertEquals("s1<1>s2</1> <b2/>s3<b3/>s3", fmt.setContent(tf).toString());
		TextContainer srcOri = new TextContainer(tf);
		
		tf = new TextFragment("s1");
		tf.append(TagType.OPENING, "b", "<b>", 1);
		tf.append("s2");
		tf.append(TagType.CLOSING, "b", "</b>", 1);
		tf.append(" ");
		tf.append(TagType.OPENING, "i", "<i>");
		tf.append("s3");
		tf.append(TagType.CLOSING, "i", "</i>");
		tf.append("s3");
		assertEquals("s1<b>s2</b> <i>s3</i>s3", tf.toText());
		assertEquals("s1<1>s2</1> <2>s3</2>s3", fmt.setContent(tf).toString());
		TextContainer trgTra = new TextContainer(tf);
		
		TextFragment res = TextUnitUtil.copySrcCodeDataToMatchingTrgCodes(srcOri.getFirstContent(),
			trgTra.getFirstContent(), true, true, null, null);
		assertEquals("s1<i>s2</i> <b>s3</i>s3<i>", res.toText());
		assertEquals("s1<1>s2</1> <2>s3</2>s3<3>", fmt.setContent(res).toString());
		//TODO: not the result we want
    }

}
