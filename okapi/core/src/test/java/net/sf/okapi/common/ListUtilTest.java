package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ListUtilTest {

	@Test
	public void testListToArray() {
		List<String> list = new ArrayList<String>();
		list.add("Blobbo");
		list.add("Cracked");
		list.add("Dumbo");
		list.add("");
		assertEquals(4, list.size());
		String[] sl = ListUtil.stringListAsArray(list);
		assertEquals(4, sl.length);
		
		List<Object> list2 = new ArrayList<Object>();
		list2.add(new String());
		list2.add(new Integer(2));
		list2.add(new Boolean(false));
		list2.add(new Boolean(true));
		list2.add(null);
		list2.add(new Character('A'));
		assertEquals(6, list2.size());
		Object[] ol = ListUtil.objectListAsArray(list2);
		assertEquals(6, ol.length);
		
		List<Class<?>> list3 = new ArrayList<Class<?>>();
		list3.add(String.class);
		list3.add(Integer.class);
		list3.add(Boolean.class);
		list3.add(null);
		list3.add(Character.class);
		assertEquals(5, list3.size());
		Class<?>[] cl = (Class<?>[]) ListUtil.classListAsArray(list3);
		assertEquals(5, cl.length);
	}

	@Test
	public void testStringAsLanguageList_Several () {
		String st = "en, fr-BE, ZU";
		List<LocaleId> list = ListUtil.stringAsLanguageList(st);
		assertEquals(3, list.size());
		assertEquals("en", list.get(0).toString());
		assertEquals("fr-BE", list.get(1).toString());
		assertEquals("zu", list.get(2).toString());
	}
	
	@Test
	public void testStringAsLanguageList_Complex () {
		String st = "  en ,,  fr-BE,\t ZU,";
		List<LocaleId> list = ListUtil.stringAsLanguageList(st);
		assertEquals(3, list.size());
		assertEquals("en", list.get(0).toString());
		assertEquals("fr-BE", list.get(1).toString());
		assertEquals("zu", list.get(2).toString());
	}
	
	@Test
	public void testStringAsLanguageList_One () {
		String st = "en-us";
		List<LocaleId> list = ListUtil.stringAsLanguageList(st);
		assertEquals(1, list.size());
		assertEquals("en-US", list.get(0).toString());
	}
	
	@Test
	public void testStringAsLanguageList_None () {
		String st = ", ,";
		List<LocaleId> list = ListUtil.stringAsLanguageList(st);
		assertEquals(0, list.size());
	}
	
//	@Test
//	public void testStringArrayAsLanguageList () {
//		String[] array = new String[]{"en","zU_ZA","CA-ES"};
//		List<LocaleId> list = ListUtil.stringArrayAsLanguageList(array);
//		assertEquals(3, list.size());
//		assertEquals("en", list.get(0).toString());
//		assertEquals("zu-za", list.get(1).toString());
//		assertEquals("ca-es", list.get(2).toString());
//	}
	
//	@Test
//	public void testLanguageListAsStringArray () {
//		List<LocaleId> list = new ArrayList<LocaleId>();
//		list.add(LocaleId.fromString("cy"));
//		list.add(LocaleId.fromString("eu_fr"));
//		list.add(LocaleId.fromString("zh_CN"));
//		String[] array = ListUtil.languageListAsStringArray(list);
//		assertNotNull(array);
//		assertEquals(3, array.length);
//		assertEquals("cy", array[0]);
//		assertEquals("eu-fr", array[1]);
//		assertEquals("zh-cn", array[2]);
//	}
	
//	@Test
//	public void testLanguageListAsString_Several () {
//		List<LocaleId> list = new ArrayList<LocaleId>();
//		list.add(LocaleId.fromString("en"));
//		list.add(LocaleId.fromString("zh_CN"));
//		list.add(LocaleId.fromString("FR-CA"));
//		String res = ListUtil.languageListAsString(list);
//		assertNotNull(res);
//		assertEquals("en,zh-cn,fr-ca", res);
//	}
	
//	@Test
//	public void testLanguageListAsString_One () {
//		List<LocaleId> list = new ArrayList<LocaleId>();
//		list.add(LocaleId.fromString("zh_CN"));
//		String res = ListUtil.languageListAsString(list);
//		assertNotNull(res);
//		assertEquals("zh-cn", res);
//	}
	
	@Test
	public void testStringAsList() {
		String st = "1,2,3,4";
		List<String> list = ListUtil.stringAsList(st);
		assertNotNull(list);
		assertEquals(4, list.size());
		assertEquals("1", list.get(0));
		assertEquals("2", list.get(1));
		assertEquals("3", list.get(2));
		assertEquals("4", list.get(3));
		
		st = "1,2,3,4,  ";
		list = ListUtil.stringAsList(st);
		assertNotNull(list);
		assertEquals(5, list.size());
		assertEquals("1", list.get(0));
		assertEquals("2", list.get(1));
		assertEquals("3", list.get(2));
		assertEquals("4", list.get(3));
		assertEquals("", list.get(4));
		
		String[] s = ListUtil.stringAsArray(st);
		assertEquals(5, s.length);
		assertEquals("1", s[0]);
		assertEquals("2", s[1]);
		assertEquals("3", s[2]);
		assertEquals("4", s[3]);
		assertEquals("", s[4]);
	}

	@Test
	public void testMerge() {
		List<String> list = Arrays.asList("aaaa", "bbb", "cccc", "ddddd");
		assertEquals("", ListUtil.merge(list, -1, -1, "-"));
		assertEquals("", ListUtil.merge(list, 100, 200, "-"));
		assertEquals("", ListUtil.merge(list, 3, 2, "-"));
		assertEquals("bbb-cccc", ListUtil.merge(list, 1, 2, "-"));
		assertEquals("bbb-cccc-ddddd", ListUtil.merge(list, 1, 3, "-"));
		assertEquals("cccc-ddddd", ListUtil.merge(list, 2, 5, "-"));
		
		String[] array = new String[] {"aaaa", "bbb", "cccc", "ddddd"};
		assertEquals("", ListUtil.merge(array, -1, -1, "-"));
		assertEquals("", ListUtil.merge(array, 100, 200, "-"));
		assertEquals("", ListUtil.merge(array, 3, 2, "-"));
		assertEquals("bbb-cccc", ListUtil.merge(array, 1, 2, "-"));
		assertEquals("bbb-cccc-ddddd", ListUtil.merge(array, 1, 3, "-"));
		assertEquals("cccc-ddddd", ListUtil.merge(array, 2, 5, "-"));		
	}

	@Test
	public void testInvert() {
		List<String> list = Arrays.asList("a", "b", "c");
		List<String> inverted = ListUtil.invert(list);
		assertEquals(Arrays.asList("c", "b", "a"), inverted);
		assertEquals(Arrays.asList("a", "b", "c"), list);
	}
}
