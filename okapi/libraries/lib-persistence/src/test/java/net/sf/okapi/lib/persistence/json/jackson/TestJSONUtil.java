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

package net.sf.okapi.lib.persistence.json.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestJSONUtil {

	@Test
	public void testToJSON() {		
		List<String> list = new ArrayList<String>();
		list.add("line 1");
		list.add("line 2");
		list.add("line 3");
		list.add("line 4");
		String json = JSONUtil.toJSON(list);
		assertEquals("{\"className\":\"java.util.ArrayList\",\"content\":[\"line 1\",\"line 2\",\"line 3\",\"line 4\"]}", json);
		
		List<Integer> list2 = new ArrayList<Integer>();
		list2.add(1);
		list2.add(2);
		list2.add(3);
		list2.add(4);
		json = JSONUtil.toJSON(list2);
		assertEquals("{\"className\":\"java.util.ArrayList\",\"content\":[1,2,3,4]}", json);
		
		List<Double> list3 = new ArrayList<Double>();
		list3.add(1.111111111);
		list3.add(2.2222);
		list3.add(3.33333333333333333333);
		list3.add(4.4444444444444444444444444444);
		json = JSONUtil.toJSON(list3);
		assertEquals("{\"className\":\"java.util.ArrayList\",\"content\":[1.111111111,2.2222,3.3333333333333335,4.444444444444445]}", json);
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("line 1", 1);
		map.put("line 2", 2);
		map.put("line 3", 3);
		map.put("line 4", 4);
		json = JSONUtil.toJSON(map);
		assertEquals("{\"className\":\"java.util.HashMap\",\"content\":{\"line 1\":1,\"line 2\":2,\"line 3\":3,\"line 4\":4}}", json);
		
		String[] array = list.toArray(new String[0]);
		json = JSONUtil.toJSON(array);
		assertEquals("{\"className\":\"[Ljava.lang.String;\",\"content\":[\"line 1\",\"line 2\",\"line 3\",\"line 4\"]}", json);
		
		String st = "line 1";
		json = JSONUtil.toJSON(st);
		assertEquals("{\"className\":\"java.lang.String\",\"content\":\"line 1\"}", json);
		
		JSONBean<String> bean = new JSONBean<String>();
		bean.setClassName("a");
		bean.setContent("d");		
		json = JSONUtil.toJSON(bean);
		assertEquals("{\"className\":\"net.sf.okapi.lib.persistence.json.jackson.JSONBean\",\"content\":{\"className\":\"a\",\"content\":\"d\"}}", json);
	}
	
	@Test
	public void testToJSON_prettyPrint() {		
		List<String> list = new ArrayList<String>();
		list.add("line 1");
		list.add("line 2");
		list.add("line 3");
		list.add("line 4");
		String json = JSONUtil.toJSON(list, true);
		assertEquals("{\n  \"className\" : \"java.util.ArrayList\",\n  \"content\" : [ \"line 1\", \"line 2\", \"line 3\", \"line 4\" ]\n}", json);
		
		List<Integer> list2 = new ArrayList<Integer>();
		list2.add(1);
		list2.add(2);
		list2.add(3);
		list2.add(4);
		json = JSONUtil.toJSON(list2, true);
		assertEquals("{\n  \"className\" : \"java.util.ArrayList\",\n  \"content\" : [ 1, 2, 3, 4 ]\n}", json);
		
		List<Double> list3 = new ArrayList<Double>();
		list3.add(1.111111111);
		list3.add(2.2222);
		list3.add(3.33333333333333333333);
		list3.add(4.4444444444444444444444444444);
		json = JSONUtil.toJSON(list3, true);
		assertEquals("{\n  \"className\" : \"java.util.ArrayList\",\n  \"content\" : [ 1.111111111, 2.2222, 3.3333333333333335, 4.444444444444445 ]\n}", json);
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("line 1", 1);
		map.put("line 2", 2);
		map.put("line 3", 3);
		map.put("line 4", 4);
		json = JSONUtil.toJSON(map, true);
		assertEquals("{\n  \"className\" : \"java.util.HashMap\",\n  \"content\" : {\n    \"line 1\" : 1,\n    \"line 2\" : 2,\n    \"line 3\" : 3,\n    \"line 4\" : 4\n  }\n}", json);
		
		String[] array = list.toArray(new String[0]);
		json = JSONUtil.toJSON(array, true);
		assertEquals("{\n  \"className\" : \"[Ljava.lang.String;\",\n  \"content\" : [ \"line 1\", \"line 2\", \"line 3\", \"line 4\" ]\n}", json);
		
		String st = "line 1";
		json = JSONUtil.toJSON(st, true);
		assertEquals("{\n  \"className\" : \"java.lang.String\",\n  \"content\" : \"line 1\"\n}", json);
		
		JSONBean<String> bean = new JSONBean<String>();
		bean.setClassName("a");
		bean.setContent("d");		
		json = JSONUtil.toJSON(bean, true);
		assertEquals("{\n  \"className\" : \"net.sf.okapi.lib.persistence.json.jackson.JSONBean\",\n  \"content\" : {\n    \"className\" : \"a\",\n    \"content\" : \"d\"\n  }\n}", json);
	}

	@Test
	public void testFromJSON() {
		String json = "{\"className\":\"java.util.ArrayList\",\"content\":[\"line 1\",\"line 2\",\"line 3\",\"line 4\"]}";
		List<?> list = JSONUtil.fromJSON(json);
		assertEquals(4, list.size());
		assertEquals("line 1", list.get(0));
		assertEquals("line 2", list.get(1));
		assertEquals("line 3", list.get(2));
		assertEquals("line 4", list.get(3));
		
		json = "{\"className\":\"java.util.ArrayList\",\"content\":[1,2,3,4]}";
		List<Integer> list2 = JSONUtil.fromJSON(json);
		assertEquals(4, list2.size());
		assertTrue(list2.get(0) == 1);
		assertTrue(list2.get(1) == 2);
		assertTrue(list2.get(2) == 3);
		assertTrue(list2.get(3) == 4);
		
		json = "{\"className\":\"java.util.ArrayList\",\"content\":[1.111111111,2.2222,3.3333333333333335,4.444444444444445]}";
		List<Double> list3 = JSONUtil.fromJSON(json);
		assertEquals(4, list3.size());
		assertTrue(list3.get(0) == 1.111111111);
		assertTrue(list3.get(1) == 2.2222);
		assertTrue(list3.get(2) == 3.3333333333333335);
		assertTrue(list3.get(3) == 4.444444444444445);
		
		json = "{\"className\":\"java.util.HashMap\",\"content\":{\"line 1\":1,\"line 3\":3,\"line 2\":2,\"line 4\":4}}";
		Map<String, Integer> map = JSONUtil.fromJSON(json);
		assertEquals(4, map.size());
		assertTrue(map.get("line 1") == 1);
		assertTrue(map.get("line 2") == 2);
		assertTrue(map.get("line 3") == 3);
		assertTrue(map.get("line 4") == 4);
		
		json = "{\"className\":\"[Ljava.lang.String;\",\"content\":[\"line 1\",\"line 2\",\"line 3\",\"line 4\"]}";
		String[] array = JSONUtil.fromJSON(json);
		assertNotNull(array);
		assertEquals(4, array.length);
		assertEquals("line 1", array[0]);
		assertEquals("line 2", array[1]);
		assertEquals("line 3", array[2]);
		assertEquals("line 4", array[3]);
		
		json = "{\"className\":\"java.lang.String\",\"content\":\"line 1\"}";
		String st = JSONUtil.fromJSON(json);
		assertEquals("line 1", st);
		
		json = "{\"className\":\"net.sf.okapi.lib.persistence.json.jackson.JSONBean\",\"content\":{\"className\":\"a\",\"content\":\"d\"}}";
		JSONBean<?> bean = JSONUtil.fromJSON(json);
		assertNotNull(bean);
		assertEquals("a", bean.getClassName());
		assertEquals("d", bean.getContent());
	}
	
	@Test
	public void testFromJSON_prettyPrint() {
		String json = "{\n  \"className\" : \"java.util.ArrayList\",\n  \"content\" : [ \"line 1\", \"line 2\", \"line 3\", \"line 4\" ]\n}";
		List<?> list = JSONUtil.fromJSON(json);
		assertEquals(4, list.size());
		assertEquals("line 1", list.get(0));
		assertEquals("line 2", list.get(1));
		assertEquals("line 3", list.get(2));
		assertEquals("line 4", list.get(3));
		
		json = "{\n  \"className\" : \"java.util.ArrayList\",\n  \"content\" : [ 1, 2, 3, 4 ]\n}";
		List<Integer> list2 = JSONUtil.fromJSON(json);
		assertEquals(4, list2.size());
		assertTrue(list2.get(0) == 1);
		assertTrue(list2.get(1) == 2);
		assertTrue(list2.get(2) == 3);
		assertTrue(list2.get(3) == 4);
		
		json = "{\n  \"className\" : \"java.util.ArrayList\",\n  \"content\" : [ 1.111111111, 2.2222, 3.3333333333333335, 4.444444444444445 ]\n}";
		List<Double> list3 = JSONUtil.fromJSON(json);
		assertEquals(4, list3.size());
		assertTrue(list3.get(0) == 1.111111111);
		assertTrue(list3.get(1) == 2.2222);
		assertTrue(list3.get(2) == 3.3333333333333335);
		assertTrue(list3.get(3) == 4.444444444444445);
		
		json = "{\n  \"className\" : \"java.util.HashMap\",\n  \"content\" : {\n    \"line 1\" : 1,\n    \"line 2\" : 2,\n    \"line 3\" : 3,\n    \"line 4\" : 4\n  }\n}";
		Map<String, Integer> map = JSONUtil.fromJSON(json);
		assertEquals(4, map.size());
		assertTrue(map.get("line 1") == 1);
		assertTrue(map.get("line 2") == 2);
		assertTrue(map.get("line 3") == 3);
		assertTrue(map.get("line 4") == 4);
		
		json = "{\n  \"className\" : \"[Ljava.lang.String;\",\n  \"content\" : [ \"line 1\", \"line 2\", \"line 3\", \"line 4\" ]\n}";
		String[] array = JSONUtil.fromJSON(json);
		assertNotNull(array);
		assertEquals(4, array.length);
		assertEquals("line 1", array[0]);
		assertEquals("line 2", array[1]);
		assertEquals("line 3", array[2]);
		assertEquals("line 4", array[3]);
		
		json = "{\n  \"className\" : \"java.lang.String\",\n  \"content\" : \"line 1\"\n}";
		String st = JSONUtil.fromJSON(json);
		assertEquals("line 1", st);
		
		json = "{\n  \"className\" : \"net.sf.okapi.lib.persistence.json.jackson.JSONBean\",\n  \"content\" : {\n    \"className\" : \"a\",\n    \"content\" : \"d\"\n  }\n}";
		JSONBean<?> bean = JSONUtil.fromJSON(json);
		assertNotNull(bean);
		assertEquals("a", bean.getClassName());
		assertEquals("d", bean.getContent());
	}
}
