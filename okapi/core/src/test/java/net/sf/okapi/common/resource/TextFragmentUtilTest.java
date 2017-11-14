package net.sf.okapi.common.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TextFragmentUtilTest {

	@Test
	public void missingCodesWithoutDeletable() {
		TextFragment src = new TextFragment();
		TextFragment trg = new TextFragment();
		src.append(new Code(TagType.PLACEHOLDER, "c", "c"));
		src.append(new Code(TagType.PLACEHOLDER, "c1", "c1"));
		src.append(new Code(TagType.PLACEHOLDER, "c2", "c2"));
		
		trg.append(new Code(TagType.PLACEHOLDER, "c", "c"));
		trg.alignCodeIds(src);
		CodeAnomalies ca = TextFragmentUtil.catalogCodeAnomalies(src, trg);
		assertNotNull(ca);
		assertEquals("c1,c2", ca.missingCodesAsString());
	}
	
	@Test
	public void missingCodesWithDuplicates() {
		TextFragment src = new TextFragment();
		TextFragment trg = new TextFragment();
		src.append(new Code(TagType.PLACEHOLDER, "c", "c"));
		src.append(new Code(TagType.PLACEHOLDER, "c1", "c1"));
		src.append(new Code(TagType.PLACEHOLDER, "c2", "c2"));
		src.append(new Code(TagType.CLOSING, "c2", "c2"));
		
		trg.append(new Code(TagType.PLACEHOLDER, "c", "c"));
		
		trg.alignCodeIds(src);
		CodeAnomalies ca = TextFragmentUtil.catalogCodeAnomalies(src, trg);
		assertNotNull(ca);
		assertEquals("c1,c2,c2", ca.missingCodesAsString());
	}
	
	@Test
	public void missingCodesWithDeletable() {
		TextFragment src = new TextFragment();
		TextFragment trg = new TextFragment();
		src.append(new Code(TagType.PLACEHOLDER, "c", "c"));
		src.append(new Code(TagType.PLACEHOLDER, "c1", "c1"));		
		src.append(new Code(TagType.PLACEHOLDER, "c2", "c2"));
		
		trg.append(new Code(TagType.PLACEHOLDER, "c", "c"));
		Code dc = new Code(TagType.PLACEHOLDER, "c2", "c2");
		dc.setDeleteable(true);
		trg.append(dc);
		
		trg.alignCodeIds(src);
		CodeAnomalies ca = TextFragmentUtil.catalogCodeAnomalies(src, trg, false);
		assertNotNull(ca);
		assertEquals("c1", ca.missingCodesAsString());
	}
	
	@Test
	public void addedCodesWithoutDeletable() {
		TextFragment src = new TextFragment();
		TextFragment trg = new TextFragment();
		src.append(new Code(TagType.PLACEHOLDER, "c", "c"));
		
		trg.append(new Code(TagType.PLACEHOLDER, "c1", "c1"));
		trg.append(new Code(TagType.PLACEHOLDER, "c2", "c2"));
		trg.append(new Code(TagType.PLACEHOLDER, "c", "c"));
		
		trg.alignCodeIds(src);
		CodeAnomalies ca = TextFragmentUtil.catalogCodeAnomalies(src, trg);
		assertNotNull(ca);
		assertEquals("c1,c2", ca.addedCodesAsString());
	}
	
	@Test
	public void addedCodesWithDeletable() {
		TextFragment src = new TextFragment();
		TextFragment trg = new TextFragment();
		src.append(new Code(TagType.PLACEHOLDER, "c", "c"));
		src.append(new Code(TagType.PLACEHOLDER, "c1", "c1"));
		
		trg.append(new Code(TagType.PLACEHOLDER, "c2", "c2"));		
		Code dc = new Code(TagType.PLACEHOLDER, "c3", "c3");
		dc.setDeleteable(true);
		trg.append(dc);
		
		trg.alignCodeIds(src);
		CodeAnomalies ca = TextFragmentUtil.catalogCodeAnomalies(src, trg, false);
		assertNotNull(ca);
		assertEquals("c2", ca.addedCodesAsString());
		assertEquals("c,c1", ca.missingCodesAsString());
	}
	
	@Test
	public void noMissingOrAddedCodes() {
		TextFragment src = new TextFragment("test");
		TextFragment trg = new TextFragment("test");
		
		trg.alignCodeIds(src);
		CodeAnomalies ca = TextFragmentUtil.catalogCodeAnomalies(src, trg);
		assertNull(ca);
	}
}
