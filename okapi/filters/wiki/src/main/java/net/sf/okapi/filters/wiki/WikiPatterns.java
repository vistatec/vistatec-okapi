package net.sf.okapi.filters.wiki;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.regex.Pattern;

public class WikiPatterns {
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Untranslatable {}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Block {
		String pair();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Inline {
		String pair();
		boolean placeholder() default false;
		String property() default "";
	}
	
	public final static IdentityHashMap<Pattern, Pattern> blockDelimiters = new IdentityHashMap<Pattern, Pattern>();;
	public final static IdentityHashMap<Pattern, Pattern> inlineDelimiters = new IdentityHashMap<Pattern, Pattern>();
	public final static IdentityHashMap<Pattern, Pattern> noWiki  = new IdentityHashMap<Pattern, Pattern>();
	public final static IdentityHashMap<Pattern, Pattern> properties = new IdentityHashMap<Pattern, Pattern>();
	
	// These should really be sets, but there is no IdentityHashSet.
	public final static IdentityHashMap<Pattern, Object> untranslatable = new IdentityHashMap<Pattern, Object>();
	public final static IdentityHashMap<Pattern, Object> startCodes = new IdentityHashMap<Pattern, Object>();
	public final static IdentityHashMap<Pattern, Object> endCodes = new IdentityHashMap<Pattern, Object>();
	
	
	/**
	 * Pattern for matching bits that we have to pull out of the text due to
	 * conflicts with other delimiters, or because their contents shouldn't be
	 * interpreted. These bits are replaced with TEMP_PLACEHOLDER.
	 */
	public static final String TEMP_EXTRACT = "%%.*?%%|"
			+ "<nowiki>.*?</nowiki>|"
			+ "\\[\\[.*?\\]\\]|"
			+ "\\{\\{.*?\\}\\}";
	public static final Pattern TEMP_EXTRACT_PATTERN = Pattern.compile(TEMP_EXTRACT);
	
	public static final String TEMP_PLACEHOLDER = "\uFFFC";

	
	/////////////////////////////////////////////////////////////////
	// Text Unit delimiters (used by ParseBlocks when passing to
	// ParseTextUnits).
	
	public static final String WHITESPACE = "(\\r?\\n[ \t]*){2,}";
	public static final Pattern WHITESPACE_PATTERN = Pattern.compile(WHITESPACE);
	
	public static final String TABLE_CELL = "[\\^|]";
	public static final Pattern TABLE_CELL_PATTERN = Pattern.compile(TABLE_CELL);
	
	
	/////////////////////////////////////////////////////////////////
	// Block delimiters
	
	public static final String LIST_ITEM_START = "^ {2,}[\\*-] |^>+ ";
	@Block(pair="LIST_ITEM_END_PATTERN")
	public static final Pattern LIST_ITEM_START_PATTERN = Pattern.compile(LIST_ITEM_START, Pattern.MULTILINE);
	public static final String LIST_ITEM_END = "\\s*\\n";
	public static final Pattern LIST_ITEM_END_PATTERN = Pattern.compile(LIST_ITEM_END);
	
	public static final String HTML_START = "(?i)<html\\b[^>]*>";
	@Block(pair="HTML_END_PATTERN")
	@Untranslatable
	public static final Pattern HTML_START_PATTERN = Pattern.compile(HTML_START);
	public static final String HTML_END = "(?i)</html>";
	public static final Pattern HTML_END_PATTERN = Pattern.compile(HTML_END);
	
	public static final String PHP_START = "(?i)<php\\b[^>]*>";
	@Block(pair="PHP_END_PATTERN")
	@Untranslatable
	public static final Pattern PHP_START_PATTERN = Pattern.compile(PHP_START);
	public static final String PHP_END = "(?i)</php>";
	public static final Pattern PHP_END_PATTERN = Pattern.compile(PHP_END);
	
	public static final String WRAP_START = "(?i)<wrap\\b[^>]*>";
	@Block(pair="WRAP_END_PATTERN")
	public static final Pattern WRAP_START_PATTERN = Pattern.compile(WRAP_START);
	public static final String WRAP_END = "(?i)</wrap>";
	public static final Pattern WRAP_END_PATTERN = Pattern.compile(WRAP_END);
	
	public static final String CODE_TAG_START = "(?i)<code\\b[^>]*>";
	@Block(pair="CODE_TAG_END_PATTERN")
	@Untranslatable
	public static final Pattern CODE_TAG_START_PATTERN = Pattern.compile(CODE_TAG_START);
	public static final String CODE_TAG_END = "(?i)</code>";
	public static final Pattern CODE_TAG_END_PATTERN = Pattern.compile(CODE_TAG_END);
	
	public static final String FILE_START = "(?i)<file\\b[^>]*>";
	@Block(pair="FILE_END_PATTERN")
	@Untranslatable
	public static final Pattern FILE_START_PATTERN = Pattern.compile(FILE_START);
	public static final String FILE_END = "(?i)</file>";
	public static final Pattern FILE_END_PATTERN = Pattern.compile(FILE_END);
	
	public static final String CODE_START = "^ {2,}(?!\\s|[\\*-])";
	@Block(pair="CODE_END_PATTERN")
	@Untranslatable
	public static final Pattern CODE_START_PATTERN = Pattern.compile(CODE_START, Pattern.MULTILINE);
	public static final String CODE_END = "\\s*\\n";
	public static final Pattern CODE_END_PATTERN = Pattern.compile(CODE_END);
	
	public static final String HEADER_START = "^={2,}";
	@Block(pair="HEADER_END_PATTERN")
	public static final Pattern HEADER_START_PATTERN = Pattern.compile(HEADER_START, Pattern.MULTILINE);
	public static final String HEADER_END = "={2,}\\s*\\n";
	public static final Pattern HEADER_END_PATTERN = Pattern.compile(HEADER_END);
	
	public static final String TABLE_START = "^\\^(?!_\\^)|^\\|";
	@Block(pair="TABLE_END_PATTERN")
	public static final Pattern TABLE_START_PATTERN = Pattern.compile(TABLE_START, Pattern.MULTILINE);
	public static final String TABLE_END = "[\\^|]\\s*\\n";
	public static final Pattern TABLE_END_PATTERN = Pattern.compile(TABLE_END);

	
	/////////////////////////////////////////////////////////////////
	// Inline code delimiters
	
	/**
	 * Zero-width match before *anything*. For one-off codes with no end delimiter.
	 */
	public static final String BOUNDARY = "(?=.)|\\z";
	
	public static final String BOLD = "\\*\\*";
	@Inline(pair="BOLD_END_PATTERN")
	public static final Pattern BOLD_START_PATTERN = Pattern.compile(BOLD);
	public static final Pattern BOLD_END_PATTERN = Pattern.compile(BOLD);
	
	public static final String UNDERLINE = "__";
	@Inline(pair="UNDERLINE_END_PATTERN")
	public static final Pattern UNDERLINE_START_PATTERN = Pattern.compile(UNDERLINE);
	public static final Pattern UNDERLINE_END_PATTERN = Pattern.compile(UNDERLINE);
	
	public static final String MONOSPACE = "''";
	@Inline(pair="MONOSPACE_END_PATTERN")
	public static final Pattern MONOSPACE_START_PATTERN = Pattern.compile(MONOSPACE);
	public static final Pattern MONOSPACE_END_PATTERN = Pattern.compile(MONOSPACE);
	
	public static final String ITALIC = "(?<!:)//|//(?=\\s|$)";
	@Inline(pair="ITALIC_END_PATTERN")
	public static final Pattern ITALIC_START_PATTERN = Pattern.compile(ITALIC);
	public static final Pattern ITALIC_END_PATTERN = Pattern.compile(ITALIC);
	
	public static final String NOWIKI = "%%";
	@Inline(pair="NOWIKI_END_PATTERN")
	public static final Pattern NOWIKI_START_PATTERN = Pattern.compile(NOWIKI);
	public static final Pattern NOWIKI_END_PATTERN = Pattern.compile(NOWIKI);
	
	public static final String NOWIKI_TAG_START = "(?i)<nowiki\\b[^>]*>";
	@Inline(pair="NOWIKI_TAG_END_PATTERN")
	public static final Pattern NOWIKI_TAG_START_PATTERN = Pattern.compile(NOWIKI_TAG_START);
	public static final String NOWIKI_TAG_END = "(?i)</nowiki>";
	public static final Pattern NOWIKI_TAG_END_PATTERN = Pattern.compile(NOWIKI_TAG_END);
	
	public static final String SUP_START = "(?i)<sup\\b[^>]*>";
	@Inline(pair="SUP_END_PATTERN")
	public static final Pattern SUP_START_PATTERN = Pattern.compile(SUP_START);
	public static final String SUP_END = "(?i)</sup>";
	public static final Pattern SUP_END_PATTERN = Pattern.compile(SUP_END);
	
	public static final String SUB_START = "(?i)<sub\\b[^>]*>";
	@Inline(pair="SUB_END_PATTERN")
	public static final Pattern SUB_START_PATTERN = Pattern.compile(SUB_START);
	public static final String SUB_END = "(?i)</sub>";
	public static final Pattern SUB_END_PATTERN = Pattern.compile(SUB_END);
	
	public static final String DEL_START = "(?i)<del\\b[^>]*>";
	@Inline(pair="DEL_END_PATTERN")
	public static final Pattern DEL_START_PATTERN = Pattern.compile(DEL_START);
	public static final String DEL_END = "(?i)</del>";
	public static final Pattern DEL_END_PATTERN = Pattern.compile(DEL_END);
	
	public static final String LINK_START = "\\[\\[[^|\\]]+\\]\\]";
	@Inline(pair="LINK_END_PATTERN", placeholder=true)
	public static final Pattern LINK_START_PATTERN = Pattern.compile(LINK_START);
	public static final Pattern LINK_END_PATTERN = Pattern.compile(BOUNDARY);
	
	public static final String NAMED_LINK_START = "\\[\\[[^|\\]]+\\|";
	@Inline(pair="NAMED_LINK_END_PATTERN")
	public static final Pattern NAMED_LINK_START_PATTERN = Pattern.compile(NAMED_LINK_START);
	public static final String NAMED_LINK_END = "\\]\\]";
	public static final Pattern NAMED_LINK_END_PATTERN = Pattern.compile(NAMED_LINK_END);
	
	public static final String IMAGE_START = "\\{\\{[^\\}]+\\}\\}";
	@Inline(pair="IMAGE_END_PATTERN", placeholder=true, property="IMAGE_CAPTION_PATTERN")
	public static final Pattern IMAGE_START_PATTERN = Pattern.compile(IMAGE_START);
	public static final Pattern IMAGE_END_PATTERN = Pattern.compile(BOUNDARY);
	
	// For capturing the image caption inside an image element ( {{img.png|Caption}} ).
	public static final String IMAGE_CAPTION = "(?<=\\|).*(?=\\}\\})";
	public static final Pattern IMAGE_CAPTION_PATTERN = Pattern.compile(IMAGE_CAPTION);
	
	public static final String FOOTNOTE_START = "\\(\\([^)]+\\)\\)";
	@Inline(pair="FOOTNOTE_END_PATTERN", placeholder=true, property="FOOTNOTE_CONTENT_PATTERN")
	public static final Pattern FOOTNOTE_START_PATTERN = Pattern.compile(FOOTNOTE_START);
	public static final Pattern FOOTNOTE_END_PATTERN = Pattern.compile(BOUNDARY);
	
	// For capturing the content of a footnote element ( ((Content)) ).
	public static final String FOOTNOTE_CONTENT = "(?<=\\(\\().*(?=\\)\\))";
	public static final Pattern FOOTNOTE_CONTENT_PATTERN = Pattern.compile(FOOTNOTE_CONTENT);
	
	public static final String HRULE_START = "-{4,}\\s*";
	@Inline(pair="HRULE_END_PATTERN", placeholder=true)
	public static final Pattern HRULE_START_PATTERN = Pattern.compile(HRULE_START);
	public static final Pattern HRULE_END_PATTERN = Pattern.compile(BOUNDARY);
	
	public static final String LINEBREAK_START = "\\\\{2,}(?:\\s+|$)";
	@Inline(pair="LINEBREAK_END_PATTERN", placeholder=true)
	public static final Pattern LINEBREAK_START_PATTERN = Pattern.compile(LINEBREAK_START);
	public static final Pattern LINEBREAK_END_PATTERN = Pattern.compile(BOUNDARY);
	
	public static final String MACRO_START = "~~(?:NOTOC|NOCACHE|INFO:\\w*)~~";
	@Inline(pair="MACRO_END_PATTERN", placeholder=true)
	public static final Pattern MACRO_START_PATTERN = Pattern.compile(MACRO_START);
	public static final Pattern MACRO_END_PATTERN = Pattern.compile(BOUNDARY);
	
	public static final String ROWSPAN_START = ":::";
	@Inline(pair="ROWSPAN_END_PATTERN", placeholder=true)
	public static final Pattern ROWSPAN_START_PATTERN = Pattern.compile(ROWSPAN_START);
	public static final Pattern ROWSPAN_END_PATTERN = Pattern.compile(BOUNDARY);
	

	static
	{
		noWiki.put(NOWIKI_START_PATTERN, NOWIKI_END_PATTERN);
		noWiki.put(NOWIKI_TAG_START_PATTERN, NOWIKI_TAG_END_PATTERN);
		
		Class<WikiPatterns> cls = WikiPatterns.class;
		
		try {
			
			for (Field f : cls.getDeclaredFields()) {
				
				if (f.getType() != Pattern.class) continue;
				
				if (f.isAnnotationPresent(Untranslatable.class))
					untranslatable.put((Pattern) f.get(null), null);
				
				if (f.isAnnotationPresent(Block.class)) {
					String pairName = f.getAnnotation(Block.class).pair();
					Pattern start = (Pattern) f.get(null);
					Pattern end = (Pattern) cls.getField(pairName).get(null);
					blockDelimiters.put(start, end);
					startCodes.put(start, null);
					endCodes.put(end, null);
				}
				
				if (f.isAnnotationPresent(Inline.class)) {
					Inline a = f.getAnnotation(Inline.class);
					String pairName = a.pair();
					Pattern start = (Pattern) f.get(null);
					Pattern end = (Pattern) cls.getField(pairName).get(null);
					inlineDelimiters.put(start, end);
					if (!a.placeholder()) {
						startCodes.put(start, null);
						endCodes.put(end, null);
					}
					if (a.property().length() != 0) {
						Pattern prop = (Pattern) cls.getField(a.property()).get(null);
						properties.put(start, prop);
					}
				}
			}
		} catch (Exception e) {
			assert(false);
			e.printStackTrace();
		}
		assert(!untranslatable.isEmpty());
		assert(!blockDelimiters.isEmpty());
		assert(!startCodes.isEmpty());
		assert(!endCodes.isEmpty());
	}
}
