package net.sf.okapi.filters.doxygen;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.filters.EventBuilder;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;

public class WhitespaceAdjustingEventBuilder extends EventBuilder {
	
	/**
	 * Matches whitespace between words that should be collapsed.
	 */
	private static final String WHITESPACE_COLLAPSE = "(?<=\\S)\\s+(?=\\S)";
	private static final Pattern WHITESPACE_COLLAPSE_PATTERN = Pattern.compile(WHITESPACE_COLLAPSE, Pattern.DOTALL);

	/**
	 * Matches whitespace surrounding a string.
	 */
	private static final String SURROUNDING_WHITESPACE = "^(\\s*)(.*?)(\\s*)$";
	private static final Pattern SURROUNDING_WHITESPACE_PATTERN = Pattern.compile(SURROUNDING_WHITESPACE, Pattern.DOTALL);
	
	public static String collapseWhitespace(String str)
	{
		Matcher m = WHITESPACE_COLLAPSE_PATTERN.matcher(str);
		
		return m.replaceAll(" ");
	}
	
	@Override
	protected ITextUnit postProcessTextUnit(ITextUnit textUnit) {
		
		if (!textUnit.isTranslatable() || textUnit.preserveWhitespaces())
			return textUnit;
		
		assert(textUnit.getSource().count() == 1);
		
		TextFragment frag = textUnit.getSource().getFirstContent();
		String text = frag.getCodedText();
		
		Matcher m = SURROUNDING_WHITESPACE_PATTERN.matcher(text);
		m.find();
		
		String frontWhitespace = m.group(1);
		String body = m.group(2);
		String backWhitespace = m.group(3);
		
		GenericSkeleton skel = (GenericSkeleton) textUnit.getSkeleton();
		if (skel == null) {
			skel = new GenericSkeleton();
			skel.addContentPlaceholder(textUnit);
			textUnit.setSkeleton(skel);
		}
		
		List<GenericSkeletonPart> parts = skel.getParts();
		
		switch (parts.size()) {
		case 1:
			parts.add(0, new GenericSkeletonPart(frontWhitespace));
			parts.add(new GenericSkeletonPart(backWhitespace));
			break;
		case 2:
			parts.get(0).append(frontWhitespace);
			parts.add(new GenericSkeletonPart(backWhitespace));
			break;
		case 3:
			parts.get(0).append(frontWhitespace);
			parts.get(2).append(backWhitespace);
			break;
		default:
			assert(false);
		}
		
		frag.setCodedText(collapseWhitespace(body));
		
		return textUnit;
	}
}
