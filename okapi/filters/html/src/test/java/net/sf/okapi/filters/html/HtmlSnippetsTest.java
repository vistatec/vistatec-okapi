package net.sf.okapi.filters.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class HtmlSnippetsTest {

	private HtmlFilter htmlFilter;
	private URL parameters;
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("fr");
	private GenericContent fmt = new GenericContent();
	private FileLocation location = FileLocation.fromClass(HtmlSnippetsTest.class);

	@Before
	public void setUp() {
		htmlFilter = new HtmlFilter();
		parameters = HtmlFilter.class.getResource("wellformedConfiguration.yml");
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testMultipleMETA() {
		String snippet = "<html title=\"Title html\">"
			+ "<meta NAME=\"keywords\" CONTENT=\"Text1\"/>"
			+ "<meta NAME=\"creation_date\" CONTENT=\"May 24, 2001\"/>"
			+ "<meta NAME=\"DESCRIPTION\" CONTENT=\"Text2\"/>"
			+ "<p>Text3</p>";
		ArrayList<Event> events = getEventsDefault(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("Title html", tu.toString());
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("Text1", tu.toString());
		tu = FilterTestDriver.getTextUnit(events, 3);
		assertNotNull(tu);
		assertEquals("Text2", tu.toString());
		tu = FilterTestDriver.getTextUnit(events, 4);
		assertNotNull(tu);
		assertEquals("Text3", tu.toString());
	}
	
	@Test
	public void testHref() {
		String snippet = "see <a href=\"http://yahoo.com\">yahoo</a>";
		ArrayList<Event> events = getEventsDefault(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("see [#$dp1]yahoo</a>", tu.toString());
	}

	@Test
	public void testInlineCodesStorage () {
		String snippet = "<p>Before <b>bold</b> <a href=\"there\"/> after.</p>";
		ArrayList<Event> events = getEventsDefault(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		List<Code> codes1 = tu.getSource().getFirstContent().getCodes();
		String tmp = Code.codesToString(codes1);
		List<Code> codes2 = Code.stringToCodes(tmp);
		assertEquals(codes1.size(), codes2.size());
		for ( int i=0; i<codes1.size(); i++ ) {
			Code code1 = codes1.get(i);
			Code code2 = codes2.get(i);
			assertEquals(code1.getData(), code2.getData());
			assertEquals(code1.getId(), code2.getId());
			assertEquals(code1.getOuterData(), code2.getOuterData());
			assertEquals(code1.getType(), code2.getType());
			assertEquals(code1.getTagType(), code2.getTagType());
			assertEquals(code1.isCloneable(), code2.isCloneable());
			assertEquals(code1.isDeleteable(), code2.isDeleteable());
		}
	}

	@Test
	public void testTitleInP () {
		String snippet = "<p title=\"Text1\">Text2</p>";
		ArrayList<Event> events = getEventsDefault(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("Text1", tu.toString());
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("Text2", tu.toString());
	}

	@Test
	public void testAltInImg () {
		String snippet = "Text1<img alt=\"Text2\"/>.";
		ArrayList<Event> events = getEventsDefault(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu); // Attributes go first
		assertEquals("Text2", tu.toString());
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("Text1<1/>.", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testNoExtractValueInInput () {
		String snippet = "<input type=\"file\" value=\"NotText\"/>.";
		ArrayList<Event> events = getEventsDefault(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("<1/>.", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testExtractValueInInput () {
		String snippet = "<input type=\"other\" value=\"Text\" placeholder=\"text-html5\"/>.";
		ArrayList<Event> events = getEventsDefault(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu);
		assertEquals("Text", tu.toString());
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("text-html5", fmt.setContent(tu.getSource().getFirstContent()).toString());
		tu = FilterTestDriver.getTextUnit(events, 3);
		assertNotNull(tu);
		assertEquals("<1/>.", fmt.setContent(tu.getSource().getFirstContent()).toString());
	}

	@Test
	public void testLabelInOption () {
		String snippet = "Text1<option label=\"Text2\"/>.";
		ArrayList<Event> events = getEventsDefault(snippet);
		ITextUnit tu = FilterTestDriver.getTextUnit(events, 1);
		assertNotNull(tu); // Attributes go first
		assertEquals("Text2", tu.toString());
		tu = FilterTestDriver.getTextUnit(events, 2);
		assertNotNull(tu);
		assertEquals("Text1", tu.toString());
		tu = FilterTestDriver.getTextUnit(events, 3);
		assertNotNull(tu);
		assertEquals(".", tu.toString());
	}

	@Test
	public void testHtmlNonWellFormedEmptyTag() {
		String snippet = "<br>text<br/>";
		URL originalParameters = parameters;
		parameters = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		ArrayList<Event> events = getEvents(snippet);
		ITextUnit tu = (ITextUnit)events.get(1).getResource();
		List<Code> codes = tu.getSource().getFirstContent().getCodes();
		for (Code code : codes) {			
			assertEquals(TagType.PLACEHOLDER, code.getTagType());
		}
		parameters = originalParameters;
	}

	@Test
	public void testAddingMETAinHTML() {
		String snippet = "<html><head></head><p>test</p></html>";
		assertEquals("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><p>test</p></html>",
			generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testAddingMETAinXHTML() {
		String snippet = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head></head><p>test</p></html>";
		assertEquals("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><p>test</p></html>",
			generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testAddingMETAinXML() {
		String snippet = "<html xmlns:MadCap=\"http://www.madcapsoftware.com/Schemas/MadCap.xsd\"><head></head><p>test</p></html>";
		assertEquals("<html xmlns:MadCap=\"http://www.madcapsoftware.com/Schemas/MadCap.xsd\"><head></head><p>test</p></html>",
			generateOutput(getEvents(snippet), snippet, locEN));
	}

	
	@Test
	public void testMETATag1() {
		String snippet = "<meta http-equiv=\"keywords\" content=\"one,two,three\"/>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testPWithAttributes() {
		String snippet = "<p title=\"my title\" dir=\"rtl\">Text of p</p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testLang() {
		String snippet = "<p lang=\"en\">Text of p</p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testLangUpdate() {
		String snippet = "<p lang=\"en\">Text <span lang=\"en\">text</span> text</p>";
		assertEquals("<p lang=\"fr\">Text <span lang=\"fr\">text</span> text</p>", generateOutput(getEvents(snippet),
				snippet, locFR));
	}

	@Test
	public void testMultilangUpdate() {
		String snippet = "<p lang=\"en\">Text</p><p lang=\"ja\">JA text</p>";
		assertEquals("<p lang=\"fr\">Text</p><p lang=\"ja\">JA text</p>", generateOutput(getEvents(snippet), snippet, locFR));
	}

	@Test
	public void testComplexEmptyElement() {
		String snippet = "<dummy write=\"w\" readonly=\"ro\" trans=\"tu1\"/>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testPWithInlines() {
		String snippet = "<p>Before <b>bold</b> <a href=\"there\"/> after.</p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testMETATag2() {
		String snippet = "<meta http-equiv=\"Content-Language\" content=\"en\"/>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testPWithInlines2() {
		String snippet = "<p>Before <img href=\"img.png\" alt=\"text\"/> after.</p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testPWithInlineTextOnly() {
		String snippet = "<p>Before <img alt=\"text\"/> after.</p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testTableGroups() {
		String snippet = "<table id=\"100\"><tr><td>text</td></tr></table>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testGroupInPara() {
		String snippet = "<p>Text before list:" + "<ul>" + "<li>Text of item 1</li>" + "<li>Text of item 2</li>"
				+ "</ul>" + "and text after the list.</p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testInput() {
		String snippet = "<p>Before <input type=\"radio\" name=\"FavouriteFare\" value=\"spam\" checked=\"checked\"/> after.</p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testCollapseWhitespaceWithPre() {
		String snippet = "<pre>   \n   \n   \t    </pre>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testCollapseWhitespaceWithoutPre() {
		String snippet = " <b>   text1\t\r\n\ftext2    </b> ";
		assertEquals("<b> text1 text2 </b>", generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testEscapedCodesInisdePre() {
		String snippet = "<pre><code>&lt;b></code></pre>";
		assertEquals("<pre><code>&lt;b></code></pre>", generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testCdataSection() {
		String snippet = "<![CDATA[&lt;b>]]>";
		assertEquals("<![CDATA[&lt;b>]]>", generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testEscapes() {
		String snippet = "<p><b>Question</b>: When the \"<code>&lt;b></code>\" code was added</p>";
		assertEquals("<p><b>Question</b>: When the &quot;<code>&lt;b></code>&quot; code was added</p>", generateOutput(
				getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testEscapedEntities() {
		String snippet = "&nbsp;M&#x0033;";
		assertEquals("\u00A0M\u0033", generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void testQuoteMode() {
		String snippet = "&quot; '";
		URL originalParameters = parameters;
		parameters = location.in("/quoteMode.yml").asUrl();
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
		parameters = originalParameters;
	}
	
	@Test
	public void testQuoteModeDefault() {
		String snippet = "&quot; '";
		URL originalParameters = parameters;
		parameters = location.in("/quoteModeDefault.yml").asUrl();
		assertEquals("&quot; &#39;", generateOutput(getEvents(snippet), snippet, locEN));
		parameters = originalParameters;
	}

	@Test
	public void testNewlineDetection() {
		String snippet = "\r\nX\r\nY\r\n";
		URL originalParameters = parameters;
		parameters = location.in("/collapseWhitespaceOff.yml").asUrl();
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
		parameters = originalParameters;
	}

	@Test
	public void testCodeFinder () {
		String snippet = "<p>text notVAR1 VAR2<p>";
		URL originalParameters = parameters;
		parameters = location.in("/withCodeFinderRules.yml").asUrl();
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		List<Code> list = tu.getSource().getFirstContent().getCodes();
		assertEquals(1, list.size());
		assertEquals("VAR2", list.get(0).getData());
		parameters = originalParameters;
	}

	@Test
	public void testCodeFinderInAttributes () {
		String snippet = "<p title='Title VAR1'>Para VAR2 <img alt='Alt VAR3'> after<p>";
		URL originalParameters = parameters;
		parameters = location.in("/withCodeFinderRules.yml").asUrl();
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1); // Title
		List<Code> list = tu.getSource().getFirstContent().getCodes();
		assertEquals(1, list.size());
		assertEquals("VAR1", list.get(0).getData());
		assertTrue(tu.getSource().getFirstContent().toString().startsWith("Title"));
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2); // Alt
		list = tu.getSource().getFirstContent().getCodes();
		assertEquals(1, list.size());
		assertEquals("VAR3", list.get(0).getData());
		assertTrue(tu.getSource().getFirstContent().toString().startsWith("Alt"));
		
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 3); // Paragraph
		list = tu.getSource().getFirstContent().getCodes();
		assertEquals(2, list.size());
		assertEquals("VAR2", list.get(1).getData());
		assertTrue(tu.getSource().getFirstContent().toString().startsWith("Para"));
		
		parameters = originalParameters;
	}

	@Test
	public void testNormalizeNewlinesInPre() {
		String snippet = "<pre>\r\nX\r\nY\r\n</pre>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testSupplementalSupport() {
		String snippet = "<p>[&#x20000;]=U+D840,U+DC00</p>";
		assertEquals("<p>[\uD840\uDC00]=U+D840,U+DC00</p>", generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testSimpleSupplementalSupport() {
		String snippet = "&#x20000;";
		assertEquals("\uD840\uDC00", generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void ITextUnitsInARow() {
		String snippet = "<td><p><h1>para text in a table element</h1></p></td>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void ITextUnitsInARowWithTwoHeaders() {
		String snippet = "<td><p><h1>header one</h1><h2>header two</h2></p></td>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void twoITextUnitsInARowNonWellformed() {
		String snippet = "<td><p><h1>para text in a table element</td>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void twoITextUnitsInARowNonWellformedWithNonWellFromedConfig() {
		URL originalParameters = parameters;
		parameters = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		String snippet = "<td><p><h1>para text in a table element</td>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
		parameters = originalParameters;
	}
	
	@Test
	public void ITextUnitName() {
		String snippet = "<p id=\"logo\">para text in a table element</p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void ITextUnitStartedWithText() {
		String snippet = "this is some text<x/>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void textUnbalancedInlineTag() {
		String snippet = "<p>this is some text</i></p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void textOverlapInlineTags() {
		String snippet = "<p><i><b>this is some text</i></b></p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void textWithUnquotedAttribtes() {
		String snippet = "<img alt=R&amp;D src=image.png>";
		assertEquals("<img alt=\"R&amp;D\" src=\"image.png\">", generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void testInlineAnchorAndAmpersand() {
		String snippet = "<a href=\"foo.cgi?chapter=1&amp;section=2&amp;copy=3&amp;lang=en\"/>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void testPAndInlineAnchorAndAmpersand() {
		String snippet = "<p>Before <a href=\"foo.cgi?chapter=1&amp;section=2&amp;copy=3&amp;lang=en\"/> after.</p>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void testCERinOutput () {
		String config = "escapeCharacters: \"\u00a0\u0104\"";
		IParameters ori = htmlFilter.getParameters();
		htmlFilter.setParameters(new Parameters(config));
		String snippet = "<p>[\u00a0\u0104]</p>";
		String expected = "<p>[&nbsp;\u0104]</p>";
		assertEquals(expected, generateOutput(getEventsDefault(snippet), snippet, locEN));
		htmlFilter.setParameters(ori);
	}

	@Test
	public void table() {
		String snippet = 
		"<table>" +
		"<tbody><tr valign=\"baseline\">" +
		"<th align=\"right\">" +
		"<strong>Subject</strong>:</th>" +
		"<td align=\"left\">" +
		"ugly <a id=\"KonaLink0\" target=\"top\" class=\"kLink\">stuff</a></td>" +
		"</tr>" +
		"</tbody></table>";
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}
	
	@Test
	public void testComplexTable() {
		String snippet = "<TABLE><TR><TD><UL><B CLASS=\"head\">Why We Exist</B><LI><B CLASS=\"side\">the problem:</B> <B CLASS=\"reason\">economic terrorism.</B></LI>" +
    "<LI><B CLASS=\"side\">terrorism=</B> <B CLASS=\"reason\">any activity not supporting Microbloat.</B></LI>" +
    "<LI><B CLASS=\"side\">example:</B> <B CLASS=\"reason\">any company competing with Microbloat.</B></LI>" +
    "<LI><B CLASS=\"side\">solution:</B> <B CLASS=\"reason\">crush the bastards while they&#39;re small.</B></LI>" +
    "</UL>" +    
    "<MENU><B CLASS=\"head\">About Our Services</B> <BR><B CLASS=\"motto\">We guarantee it!!</B><LI><A HREF=\"error_notloggedin.html\">conditions of use</A></LI>" +
   "<LI><A HREF=\"error_notloggedin.html\">privacy policy</A></LI>" +
   "</MENU>" +
   "</TD></TR></TABLE>";
		URL originalParameters = parameters;
		parameters = HtmlFilter.class.getResource("nonwellformedConfiguration.yml");
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
		parameters = originalParameters;
	}

	@DataProvider
	public static Object[][] testTextDirectionClarificationProvider() {
		String rtlExpectedOutput = "<!DOCTYPE html>\n"
				+ "<html dir=\"rtl\">\n"
				+ "<body>\n"
				+ "<p>بعض الكلمات</p>\n"
				+ "</body>\n"
				+ "</html>\n";
		String ltrExpectedOutput = "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<body>\n"
				+ "<p>Some text.</p>\n"
				+ "</body>\n"
				+ "</html>\n";
		String rtlExpectedOutputWithTranslateAttribute = "<!DOCTYPE html>\n"
				+ "<html dir=\"rtl\" translate=\"no\">\n"
				+ "<body>\n"
				+ "<p>بعض الكلمات</p>\n"
				+ "</body>\n"
				+ "</html>\n";
		return new Object[][] {
				{
						"<!DOCTYPE html>\n"
								+ "<html dir=\"rtl\">\n"
								+ "<body>\n"
								+ "<p>بعض الكلمات</p>\n"
								+ "</body>\n"
								+ "</html>\n",
						LocaleId.ARABIC,
						rtlExpectedOutput,
				},
				{
						"<!DOCTYPE html>\n"
								+ "<html dir=\"ltr\">\n"
								+ "<body>\n"
								+ "<p>بعض الكلمات</p>\n"
								+ "</body>\n"
								+ "</html>\n",
						LocaleId.ARABIC,
						rtlExpectedOutput,
				},
				{
						"<!DOCTYPE html>\n"
								+ "<html>\n"
								+ "<body>\n"
								+ "<p>بعض الكلمات</p>\n"
								+ "</body>\n"
								+ "</html>\n",
						LocaleId.ARABIC,
						rtlExpectedOutput,
				},
				{
						"<!DOCTYPE html>\n"
								+ "<html dir=\"rtl\">\n"
								+ "<body>\n"
								+ "<p>Some text.</p>\n"
								+ "</body>\n"
								+ "</html>\n",
						LocaleId.ENGLISH,
						ltrExpectedOutput,
				},
				{
						"<!DOCTYPE html>\n"
								+ "<html dir=\"ltr\">\n"
								+ "<body>\n"
								+ "<p>Some text.</p>\n"
								+ "</body>\n"
								+ "</html>\n",
						LocaleId.ENGLISH,
						ltrExpectedOutput,
				},
				{
						"<!DOCTYPE html>\n"
								+ "<html>\n"
								+ "<body>\n"
								+ "<p>Some text.</p>\n"
								+ "</body>\n"
								+ "</html>\n",
						LocaleId.ENGLISH,
						ltrExpectedOutput,
				},
				{
						"<!DOCTYPE html>\n"
								+ "<html translate=\"no\">\n"
								+ "<body>\n"
								+ "<p>بعض الكلمات</p>\n"
								+ "</body>\n"
								+ "</html>\n",
						LocaleId.ARABIC,
						rtlExpectedOutputWithTranslateAttribute,
				},
		};
	}

	@Test
	@UseDataProvider("testTextDirectionClarificationProvider")
	public void testTextDirectionClarification(String snippet, LocaleId locale, String expected) throws Exception {
		assertEquals(expected, FilterTestDriver.generateOutput(getEvents(snippet), locale,
				htmlFilter.createSkeletonWriter(), htmlFilter.getEncoderManager()));
	}

	@Test
	public void testTranslateAttribute() {
		String snippet = "<p>text with a <span translate='no'>no-translation part</span> and more.</p>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("text with a  and more.", tu.getSource().getFirstContent().toString());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertNull(tu);
	}
	
	@Test @Ignore
	public void disabled_testPBlockTranslateAttribute() {
		String snippet = "<p translate='no'>no trans</p><p>Text <span translate='no'>no-trans</span></p>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("Text ", tu.getSource().getFirstContent().toString());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertNull(tu);
	}
	
	@Test
	public void testDivBlockTranslateAttribute() {
		String snippet = "<div translate='no'>no trans</div><p>Text <span translate='no'>no-trans</span></p>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals("Text ", tu.getSource().getFirstContent().toString());
		tu = FilterTestDriver.getTextUnit(getEvents(snippet), 2);
		assertNull(tu);
	}
	
	@Test
	public void testFreeMarker() {
		String snippet = "<strong> this is a bolded text between html strong tags </strong> <#if contactInfo??> or ${contactInfo}</#if>.";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertEquals(" this is a bolded text between html strong tags  <#if contactInfo??> or ${contactInfo}</#if>.", tu.getSource().getFirstContent().toString());
	}
	
	@Test
	public void testPlaceholderOnlySegments() {
		String snippet = "<table><tr><td><br/></td></tr><tr><td><img src='...'></td></tr></table>";
		List<Event> events = getEvents(snippet);
		assertEquals(0, FilterTestDriver.countEventsByType(events, EventType.TEXT_UNIT));
	}
	
	@Test @Ignore
	public void disabled_testDivBlockExcludeIncludeTranslateAttribute() {
		String snippet = "<div translate='no'>no <div translate='yes'>trans</div></div>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("trans", tu.getSource().getFirstContent().toString());
	}
	
	@Test
	public void testTagLowerCaseFix() {
		// StreamedSourceCopy was lower casing all element names that had
		// attributes without quotes
		String snippet = "<B><FONT SIZE=3>our accomplishments</B></FONT>";
		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
		assertNotNull(tu);
		assertEquals("<B><FONT SIZE=\"3\">our accomplishments</B></FONT>", tu.getSource().getFirstContent().toText());
	}
	
	@Test
	public void testInlineCdata() {
		URL originalParameters = parameters;
		String config = "inlineCdata: true";
		htmlFilter.setParameters(new Parameters(config));
		String snippet = "Here is some <![CDATA[inline cdata<>&]]> for you.";
		List<Event> events = getEventsDefault(snippet);
		assertEquals(events.size(), 3);
		TextFragment frag = FilterTestDriver.getTextUnit(events, 1).getSource().getFirstContent();
		assertEquals("Here is some inline cdata<>& for you.", frag.getCodedText());
		List<Code> codes = frag.getCodes();
		assertEquals("<![CDATA[", codes.get(0).getData());
		assertEquals(Code.TYPE_CDATA, codes.get(0).getType());
		assertEquals(TagType.OPENING, codes.get(0).getTagType());
		assertEquals("]]>", codes.get(1).getData());
		assertEquals(Code.TYPE_CDATA, codes.get(1).getType());
		assertEquals(TagType.CLOSING, codes.get(1).getTagType());
		parameters = originalParameters;
	}
	
	@Test
	public void testEmptyGroupAtEnd () {
		String config = 
			"elements:\n" +
			"  g:\n" +
			"    ruleTypes: [GROUP]";
		IParameters ori = htmlFilter.getParameters();
		htmlFilter.setParameters(new Parameters(config));
		String snippet = "Empty group at the end <g/>";
		assertEquals(snippet, generateOutput(getEventsDefault(snippet), snippet, locEN));
		htmlFilter.setParameters(ori);
	}
	
	@Test
	public void testASPXComment () {
		String snippet = "<%-- comment --%>Text";
		TextFragment frag = FilterTestDriver.getTextUnit(getEventsDefault(snippet), 1).getSource().getFirstContent();
		assertEquals("Text", frag.getText());
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	@Test
	public void testASPXEmbeddedTag () {
		String snippet = "<asp:Label ID=\"Label4\" runat=\"server\" Text=\"<%$ Resources:website, Home %>\"></asp:Label>Text";
		TextFragment frag = FilterTestDriver.getTextUnit(getEventsDefault(snippet), 1).getSource().getFirstContent();
		assertEquals("Text", frag.getText());
		assertEquals(snippet, generateOutput(getEvents(snippet), snippet, locEN));
	}

	private ArrayList<Event> getEventsDefault(String snippet) {
		ArrayList<Event> list = new ArrayList<Event>();
		// Use default parameters
		htmlFilter.open(new RawDocument(snippet, locEN));
		while (htmlFilter.hasNext()) {
			Event event = htmlFilter.next();
			list.add(event);
		}
		htmlFilter.close();
		return list;
	}

	private ArrayList<Event> getEvents(String snippet) {
		return FilterTestDriver.getEvents(htmlFilter, snippet, new Parameters(parameters), locEN, null);
	}

	private String generateOutput(ArrayList<Event> list, String original, LocaleId trgLang) {
		GenericSkeletonWriter writer = new GenericSkeletonWriter();
		StringBuilder tmp = new StringBuilder();
		for (Event event : list) {
			switch (event.getEventType()) {
			case START_DOCUMENT:
				writer.processStartDocument(trgLang, "utf-8", null, htmlFilter.getEncoderManager(), (StartDocument) event
						.getResource());
				break;
			case TEXT_UNIT:
				ITextUnit tu = (ITextUnit) event.getResource();
				tmp.append(writer.processTextUnit(tu));
				break;
			case DOCUMENT_PART:
				DocumentPart dp = (DocumentPart) event.getResource();
				tmp.append(writer.processDocumentPart(dp));
				break;
			case START_GROUP:
			case START_SUBFILTER:
				StartGroup startGroup = (StartGroup) event.getResource();
				tmp.append(writer.processStartGroup(startGroup));
				break;
			case END_GROUP:
			case END_SUBFILTER:
				Ending ending = (Ending) event.getResource();
				tmp.append(writer.processEndGroup(ending));
				break;
			default:
				break;
			}
		}
		writer.close();
		return tmp.toString();
	}

}
