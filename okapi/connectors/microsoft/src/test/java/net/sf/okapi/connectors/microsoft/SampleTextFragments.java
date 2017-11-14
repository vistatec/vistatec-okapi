package net.sf.okapi.connectors.microsoft;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

public class SampleTextFragments {

	public static TextFragment makeSourceTextFragment() {
		TextFragment tf = new TextFragment();
		tf.append(new Code(TagType.OPENING, "p", "<p>"));
		tf.append("Cats and ");
		tf.append(new Code(TagType.OPENING, "<b>", "<b>"));
		tf.append("dogs");
		tf.append(new Code(TagType.CLOSING, "</b>", "</b>"));
		tf.append(" & ");
		tf.append(new Code(TagType.OPENING, "<i>", "<i>"));
		tf.append("skunks");
		tf.append(new Code(TagType.CLOSING, "</i>", "</i>"));
		tf.append(".");
		tf.append(new Code(TagType.CLOSING, "p", "</p>"));
		return tf;
	}
	public static TextFragment makeTargetTextFragment() {
		TextFragment tf = new TextFragment();
		tf.append(new Code(TagType.OPENING, "p", "<p>"));
		tf.append("Chats et ");
		tf.append(new Code(TagType.OPENING, "<b>", "<b>"));
		tf.append("chiens");
		tf.append(new Code(TagType.CLOSING, "</b>", "</b>"));
		tf.append(" & ");
		tf.append(new Code(TagType.OPENING, "<i>", "<i>"));
		tf.append("mouffettes");
		tf.append(new Code(TagType.CLOSING, "</i>", "</i>"));
		tf.append(".");
		tf.append(new Code(TagType.CLOSING, "p", "</p>"));
		return tf;
	}
}
