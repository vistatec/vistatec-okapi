package net.sf.okapi.filters.xini.rainbowkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.xini.jaxb.Element;
import net.sf.okapi.filters.xini.jaxb.Field;
import net.sf.okapi.filters.xini.jaxb.Seg;
import net.sf.okapi.filters.xini.jaxb.TextContent;
import net.sf.okapi.filters.xini.jaxb.Trans;
import net.sf.okapi.filters.xini.jaxb.Xini;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FilterEventsToXiniTransformerTest {

	private static final String DUMMY_FIELD_LABEL_CONTENT = "test_field_content";
	private static final String TARGET_TEST_STRING = "Übersetzter test String";
	private static final String SOURCE_TEST_STRING = "test String";
	private static final String NBSP_STRING = "\u00A0";

	private FilterEventToXiniTransformer transformer;
	private TextUnit txtUnit1;
	private Xini xini;

	@Before
	public void setUp() {
		transformer = new FilterEventToXiniTransformer();
		transformer.init();
		transformer.startPage("TestPage1");

		txtUnit1 = new TextUnit("TU1");

		TextContainer textContainer = createTextContainer("1",
				SOURCE_TEST_STRING);
		txtUnit1.setId("1");
		txtUnit1.setSource(textContainer);
		textContainer = createTextContainer("1", TARGET_TEST_STRING);
		txtUnit1.setTarget(new LocaleId("de"), textContainer);
		textContainer = createTextContainer("1", TARGET_TEST_STRING + "_EN");
		txtUnit1.setTarget(new LocaleId("en"), textContainer);

		// root = TestUtil.getParentDir(this.getClass(), "/test01.xml");
	}

	private TextContainer createTextContainer(String segId, String fragmentText) {
		TextFragment textFragment = new TextFragment(fragmentText);
		Segment segment = new Segment(segId, textFragment);
		return new TextContainer(segment);
	}

	@Test
	public void exportsPreTranslations() {
		transformer.transformTextUnit(txtUnit1);
		Xini xini = getXiniFrom(transformer);

		List<Seg> segs = getSegListFromFirstElement(xini);
		assertEquals(1, segs.size());
		List<TextContent> segAndTrans = getSegAndTransListFromFirstElement(xini);
		TextContent sourceCont = null;
		TextContent targetContEN = null;
		TextContent targetContDE = null;
		for (TextContent context : segAndTrans) {
			if (context instanceof Seg) {
				sourceCont = context;
			} else if ((context instanceof Trans) && "en".equalsIgnoreCase(((Trans)context).getLanguage())) {
				targetContEN = context;
			} else {
				targetContDE = context;
			}
		}

		assertEquals(3, segAndTrans.size());
		assertTrue(sourceCont.getContent().contains(SOURCE_TEST_STRING));
		assertTrue(targetContEN.getContent().contains(
				TARGET_TEST_STRING + "_EN"));

		assertTrue(targetContDE.getContent().contains(TARGET_TEST_STRING));

		// writeOutForTest();
	}

	@Test
	public void exportsNonBreakingSpaceAsEmptyTranslation() {
		this.setUpTextUnitWithNbspContent();

		transformer.transformTextUnit(txtUnit1);

		assertEmptyTranslationFlagSet();
	}

	@Test
	public void xiniFieldStoresFieldLabelFromTuProperty() {
		txtUnit1.setProperty(new Property(FilterEventToXiniTransformer.FIELD_LABEL_PROPERTY, DUMMY_FIELD_LABEL_CONTENT));

		transformer.transformTextUnit(txtUnit1);
		Field field = getFirstField();

		assertEquals(DUMMY_FIELD_LABEL_CONTENT, field.getLabel());
	}

	@Test
	public void xiniFieldIsNullIfTuHasNoProperty() {
		transformer.transformTextUnit(txtUnit1);
		Field field = getFirstField();

		assertEquals(null, field.getLabel());
	}

	@Test
	public void xiniFieldStoresFieldLabelFromStartGroupProperty() {
		StartGroup startGroup = createStartGroup(DUMMY_FIELD_LABEL_CONTENT);
		transformer.pushGroupToStack(startGroup);
		transformer.transformTextUnit(txtUnit1);
		Field field = getFirstField();

		assertEquals(DUMMY_FIELD_LABEL_CONTENT, field.getLabel());
	}

	@Test
	public void labelFromOuterStartGroupIsOveriddenByInnerStartGroup() {
		StartGroup outerStartGroup = createStartGroup(DUMMY_FIELD_LABEL_CONTENT);
		transformer.pushGroupToStack(outerStartGroup);

		String innerProperty = "inner_property";
		StartGroup innerStartGroup = createStartGroup(innerProperty);
		transformer.pushGroupToStack(innerStartGroup);

		transformer.transformTextUnit(txtUnit1);
		Field field = getFirstField();

		assertEquals(innerProperty, field.getLabel());
	}

	@Test
	public void labelFromOuterStartGroupIsUsedAfterEndingInnerGroup() {
		String outerProperty = "outer_property";
		StartGroup outerStartGroup = createStartGroup(outerProperty);
		transformer.pushGroupToStack(outerStartGroup);

		String innerProperty = "inner_property";
		StartGroup innerStartGroup = createStartGroup(innerProperty);
		transformer.pushGroupToStack(innerStartGroup);
		transformer.popGroupFromStack();

		transformer.transformTextUnit(txtUnit1);
		Field field = getFirstField();

		assertEquals(outerProperty, field.getLabel());
	}

	@Test
	public void labelFromStartGroupGetsResetByEndGroup() {
		StartGroup startGroup = createStartGroup(DUMMY_FIELD_LABEL_CONTENT);
		transformer.pushGroupToStack(startGroup);
		transformer.popGroupFromStack();
		transformer.transformTextUnit(txtUnit1);
		Field field = getFirstField();

		assertEquals(null, field.getLabel());
	}

	private StartGroup createStartGroup(String fieldLabel) {
		StartGroup startGroup = new StartGroup(null);
		startGroup.setProperty(new Property(FilterEventToXiniTransformer.FIELD_LABEL_PROPERTY, fieldLabel));
		return startGroup;
	}

	private Field getFirstField() {
		final Xini xini = getXiniFrom(transformer);
		final Element firstElement = this.getFirstElementFromXini(xini);
		Field field = firstElement.getElementContent().getFields().getField().get(0);
		return field;
	}

	private void setUpTextUnitWithNbspContent() {
		txtUnit1 = new TextUnit("TU1");

		TextContainer textContainer = createTextContainer("1", NBSP_STRING);
		txtUnit1.setId("1");
		txtUnit1.setSource(textContainer);
	}

	private void assertEmptyTranslationFlagSet() {
		Field field = getFirstField();
		Seg seg = field.getSeg().get(0);
		assertTrue(seg.isEmptyTranslation());
	}

	private List<Seg> getSegListFromFirstElement(Xini xini2) {
		Element element = getFirstElementFromXini(xini);

		return element.getElementContent().getFields().getField().get(0)
				.getSeg();
	}

	private List<TextContent> getSegAndTransListFromFirstElement(Xini xini) {
		Element element = getFirstElementFromXini(xini);
		return element.getElementContent().getFields().getField().get(0)
				.getSegAndTrans();
	}

	private Element getFirstElementFromXini(Xini xini) {
		Element element = xini.getMain().getPage().get(0).getElements()
				.getElement().get(0);
		return element;
	}

	private Xini getXiniFrom(FilterEventToXiniTransformer transformer2) {
		try {
			java.lang.reflect.Field xiniField = transformer.getClass().getDeclaredField("xini");
			xiniField.setAccessible(true);

			return xini = (Xini) xiniField.get(transformer);

		}
		catch (SecurityException e) {
			throw new OkapiException(e);
		}
		catch (NoSuchFieldException e) {
			throw new OkapiException(e);
		}
		catch (IllegalArgumentException e) {
			throw new OkapiException(e);
		}
		catch (IllegalAccessException e) {
			throw new OkapiException(e);
		}
	}

}
