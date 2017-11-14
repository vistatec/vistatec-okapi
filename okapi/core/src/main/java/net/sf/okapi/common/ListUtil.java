/*===========================================================================
  Copyright (C) 2009-2017 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods to manipulate lists.
 */
public class ListUtil {
	
	/**
	 * Splits up a string of comma-separated substrings into a string list of those substrings.
	 * @param st string of comma-separated substrings. 
	 * @return a list of substrings.
	 */
	public static List<String> stringAsList(String st) {
		return listTrimValues(stringAsList(st, ","));		
	}
	
//	/**
//	 * Converts an array of string representing locales into a list of locales.
//	 * @param array the array of strings to convert.
//	 * @return a list of locales for the given strings.
//	 */
//	public static List<LocaleId> stringArrayAsLanguageList (String[] array) {
//		List<LocaleId> list = new ArrayList<LocaleId>();
//		for ( int i=0; i<array.length; i++ ) {			
//			list.add(LocaleId.fromString(array[i]));
//		}
//		return list;
//	}

	/**
	 * Splits up a string of comma-separated substrings representing locale codes into a
	 * string list of {@link LocaleId} objects.
	 * @param input string of comma-separated substrings. 
	 * @return a list of {@link LocaleId} objects.
	 */
	public static List<LocaleId> stringAsLanguageList (String input) {
		if ( input == null ) return null;
		List<LocaleId> res = new ArrayList<LocaleId>();
		List<String> list = new ArrayList<String>();
		stringAsList(list, input, ",");
		for ( String lang : list ) {
			lang = lang.trim();
			if ( !Util.isEmpty(lang) ) {
				res.add(LocaleId.fromString(lang));
			}
		}
		return res;
	}

//	/**
//	 * Converts a list of languages into an array of strings.
//	 * @param list List of languages.
//	 * @return an array of strings for the given languages.
//	 */
//	public static String[] languageListAsStringArray (List<LocaleId> list) {
//		String[] res = new String[list.size()];
//		for ( int i=0; i<list.size(); i++ ) {			
//			res[i] = list.get(i).toString();
//		}
//		return res;
//	}
	
//	/**
//	 * Creates a string output for a list of languages. The language identifiers
//	 * are separated by commas.
//	 * @param list the list of languages to convert.
//	 * @return the output string.
//	 */
//	public static String languageListAsString (List<LocaleId> list) {
//		if ( list == null ) return "";
//		StringBuilder tmp = new StringBuilder();
//		for ( int i=0; i<list.size(); i++ ) {
//			if ( i > 0 ) {
//				tmp.append(",");
//			}
//			tmp.append(list.get(i).toString());
//		}
//		return tmp.toString();
//	}
	
	/**
	 * Splits up a string of comma-separated substrings into a string list of those substrings.
	 * @param list a list to put the substrings.
	 * @param st string of comma-separated substrings. 
	 */
	public static void stringAsList(List<String> list, String st) {
		stringAsList(list, st, ",");
		listTrimValues(list);
	}
	
	/**
	 * Splits up a string of delimited substrings into a string list of those substrings.
	 * @param st string of delimited substrings.
	 * @param delimiter a string delimiting substrings in the string. 
	 * @return a list of substrings.
	 */
	public static List<String> stringAsList(String st, String delimiter) {
		ArrayList<String> res = new ArrayList<String>();		
		stringAsList(res, st, delimiter);		
		return res;		
	}
	
	/**
	 * Splits up a string of delimited substrings into a string list of those substrings.
	 * @param st string of delimited substrings.
	 * @param delimiter a character delimiting substrings in the string. 
	 * @return a list of substrings.
	 */
	public static List<String> stringAsList(String st, char delimiter) {
		ArrayList<String> res = new ArrayList<String>();
		stringAsList(res, st, Character.toString(delimiter));		
		return res;		
	}
	
	/**
	 * Splits up a string of delimited substrings into a string list of those substrings.
	 * @param list a list to put the substrings.
	 * @param st string of delimited substrings.
	 * @param delimiter a string delimiting substrings in the string.	  
	 */
	public static void stringAsList(List<String> list, String st, String delimiter) {
		if (Util.isEmpty(st)) return;
		if (list == null) return;
		
		list.clear();
		
		if (Util.isEmpty(delimiter)) {
						
			list.add(st);
			return;
		}
	
		int start = 0;
		int len = delimiter.length();
		
		while (true) {
			
			int index = st.substring(start).indexOf(delimiter);
			if (index == -1) break;
			
			list.add(st.substring(start, start + index));
			start += index + len;
		}
		
		if (start <= st.length())
			list.add(st.substring(start, st.length()));		
	}
	
	/**
	 * Splits up a string of comma-separated substrings into an array of those substrings.
	 * @param st string of comma-separated substrings.
	 * @return the generated array of strings.
	 */
	public static String[] stringAsArray(String st) {
		List<String> list = stringAsList(st);
		
		if (Util.isEmpty(list))
			return new String[] {};
		
		return (String[]) list.toArray(new String[] {});
	}
	
	/**
	 * Splits up a string of comma-separated substrings into an array of those substrings.
	 * @param st string of comma-separated substrings.
	 * @param delimiter a string delimiting substrings in the string.
	 * @return the generated array of strings.
	 */
	public static String[] stringAsArray(String st, String delimiter) {
		List<String> list = stringAsList(st, delimiter);
		
		if (Util.isEmpty(list))
			return new String[] {};

		return (String[]) list.toArray(new String[] {});
	}
	
	/**
	 * Merges specified elements of a given string array into a single string. The merged elements are joined with a given joiner.
	 * @param array the given array of strings.
	 * @param start index of the start element to be merged.
	 * @param end index of the end element (inclusive) to be merged.
	 * @param joiner string to join elements in the resulting string.
	 * @return the string of merged elements.
	 */
	public static String merge(String[] array, int start, int end, String joiner) {		
		//return merge(Arrays.asList(array), start, end, joiner);
		
		if (!Util.checkIndex(start, array) && !Util.checkIndex(end, array))
			return "";
			
		if (start < 0 && Util.checkIndex(end, array))
			start = 0;
		
		if (Util.checkIndex(start, array) && end >= array.length)
			end = array.length - 1;
		
		if (start >= end) return "";
		
		StringBuilder tmp = new StringBuilder(array[start]);
		
		for (int i = start + 1; i < end + 1; i++) {
			
			tmp.append(joiner);
			tmp.append(array[i]);			
		}
		
		return tmp.toString();
	}
	
	/**
	 * Merges specified items of a given string list into a single string. The merged items are joined with a given joiner.
	 * @param list the given list of strings.
	 * @param start index of the start item to be merged.
	 * @param end index of the end item (inclusive) to be merged.
	 * @param joiner string to join items in the resulting string.
	 * @return the string of merged items.
	 */
	public static String merge(List<String> list, int start, int end, String joiner) {
		
		return merge(stringListAsArray(list), start, end, joiner);		
	}
	
	/**
	 * Converts a string of comma-separated numbers into a list of integers.
	 * @param st string of comma-separated numbers. 
	 * @return a list of integers.
	 */
	public static List<Integer> stringAsIntList (String st) {
		return stringAsIntList(st, ",");
	}
	
	/**
	 * Converts a string of comma-separated numbers into a list of integers and sorts the list ascendantly.
	 * @param st string of comma-separated numbers 
	 * @param delimiter a string delimiting numbers in the string
	 * @return a list of integers 
	 */
	public static List<Integer> stringAsIntList(String st, String delimiter) {
		return stringAsIntList(st, delimiter, false);
	}		
	
	/**
	 * Converts a string of comma-separated numbers into a list of integers.
	 * @param st string of comma-separated numbers 
	 * @param delimiter a string delimiting numbers in the string
	 * @param sortList if the numbers in the resulting list should be sorted (ascendantly)
	 * @return a list of integers 
	 */
	public static List<Integer> stringAsIntList(String st, String delimiter, boolean sortList) {
		List<Integer> res = new ArrayList<Integer>(); // Always create the list event if input string is empty
		if (Util.isEmpty(st)) return res;
		
		String[] parts = st.split(delimiter);
		for (String part : parts) {
			
			if (Util.isEmpty(part.trim()))
				res.add(0);
			else
				res.add(Integer.valueOf(part.trim()));
		}
		if (sortList) Collections.sort(res);
		return res;
	}
	
	/**
	 * Remove empty trailing elements of the given list.
	 * Possible empty elements in the head and middle of the list remain if located before a non-empty element.
	 * @param list the list to be trimmed.
	 */
	public static void listTrimTrail(List<String> list) {
	
		if (list == null) return;

		for (int i = list.size() -1; i >= 0; i--) 
			if (Util.isEmpty(list.get(i))) 
				list.remove(i);
			else
				break;
	}
	
	/**
	 * Creates a list, containing all trimmed values of a given list of strings. Empty elements remain on the 
	 * list, non-empty ones are trimmed from both sides. The given list is not changed.
	 * @param list the given list of strings.
	 * @return the list with trimmed elements.
	 */
	public static List<String> listTrimValues(List<String> list) {
		if ( list == null ) return null;
		List<String> res = new ArrayList<String>();
		
		for (String st : list)
			if (Util.isEmpty(st)) 
				res.add(st);
			else
				res.add(st.trim());
			
		return res;
	}

	/**
	 * Converts a list of strings into an array of those strings.
	 * @param list List of strings.
	 * @return an array of strings.
	 */
	public static String[] stringListAsArray(List<String> list) {
		if (Util.isEmpty(list))
			return new String[] {};
		
		return (String[]) list.toArray(new String[] {});
	}
	
	/**
	 * Converts a list of class references into an array of those class references.
	 * @param list List of class references.
	 * @return An array of class references.
	 */
	public static Class<?>[] classListAsArray(List<Class<?>> list) {
		if (Util.isEmpty(list))
			return null;
		
		Class<?>[] res = (Class<?>[]) Array.newInstance(Class.class, list.size());
		for (int i = 0; i < list.size(); i++) 
			res[i] = list.get(i);
		
		return res;
	}
	
	/**
	 * Converts a list of objects into an array of those objects.
	 * @param list List of objects.
	 * @return an array of objects.
	 */
	public static Object[] objectListAsArray(List<Object> list) {
		if (Util.isEmpty(list))
			return null;
		
		return list.toArray();
	}
	
	/**
	 * Converts a given array to a list containing all elements of the original array. The resulting list is 
	 * not backed by the given array. (Changes to the returned list DO NOT "write through" to the array and vice versa.)  
	 * @param <E> the type of the array's element.
	 * @param array the given array.
	 * @return the list of all elements contained in the given array.
	 */
	public static <E> List<E> arrayAsList(E[] array) {
		return new ArrayList<E>(Arrays.asList(array));
	}
	
	/**
	 * Returns a string, representing a given array of strings. Array elements are separated with a comma in 
	 * the resulting string.
	 * @param array the given array of strings.
	 * @return the string with comma-separated elements of the given array.
	 */
	public static String arrayAsString(String[] array) {
		return arrayAsString(array, ",");
	}
	
	/**
	 * Returns a string, representing a given array of strings. Array elements in the resulting string are separated with a given 
	 * delimiter.
	 * @param array the given array of strings.
	 * @param delimiter the given delimiter.
	 * @return the string with delimited elements of the given array.
	 */
	public static String arrayAsString(String[] array, String delimiter) {
		return listAsString(Arrays.asList(array), delimiter);
	}
	
	/**
	 * Returns a string, representing a given list of strings. List elements are separated with a comma in 
	 * the resulting string.
	 * @param list the given list of strings.
	 * @return the string with comma-separated elements of the given list.
	 */
	public static String listAsString(List<String> list) {
		return listAsString(list, ",");
	}
	
	/**
	 * Returns a string, representing a given list of strings. List elements in the resulting string are separated with a given 
	 * delimiter.
	 * @param list the given list of strings.
	 * @param delimiter the given delimiter.
	 * @return the string with delimited elements of the given list.
	 */
	public static String listAsString(List<String> list, String delimiter) {
		if (list == null) return "";
		String res = "";
		
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) 
				res = res + delimiter + list.get(i);
			else
				res = list.get(i);			
		}
		
		return res;
	}
	
	/**
	 * Returns a string, representing a given list of integers. List elements are converted into strings ("12" for 12 etc.) and
	 * separated with a comma in the resulting string.
	 * @param list the given list of integers.
	 * @return the string with comma-separated converted elements of the given list.
	 */
	public static String intListAsString(List<Integer> list) {
		return intListAsString(list, ",");
	}
	
	/**
	 * Returns a string, representing a given list of integers. List elements are converted into strings ("12" for 12 etc.) and 
	 * separated with a given delimiter.
	 * @param list the given list of integers.
	 * @param delimiter the given delimiter.
	 * @return the string with delimited converted elements of the given list.
	 */
	public static String intListAsString(List<Integer> list, String delimiter) {
		List<String> stList = new ArrayList<String>();
		for (Integer value : list) {
			stList.add(Util.intToStr(value));
		}
		return listAsString(stList, delimiter);
	}

	/**
	 * Returns a copy of the specified list in reverse order. Differs from
	 * <tt>Collections#reverse</tt> in that it does not modify the original list.
	 * @param list the list to reverse.
	 * @return a copy of the reverted list.
	 */
	public static <E> List<E> invert(List<E> list) {
		if (list == null) return null;
		List<E> res = new ArrayList<>(list);
		Collections.reverse(res);
		return res;
	}
	
	/**
	 * Removes a range of elements from a given list.
	 * @param <E> the type of the list's element.
	 * @param list the given list.
	 * @param start the start index (inclusive) to be removed.
	 * @param end the end index (exclusive) to be removed.
	 */
	public static <E> void remove(List<E> list, int start, int end) {
		
		if (list == null) return;
		if (Util.isEmpty(list)) return;
		
		//for (int i = start; (i >= 0 && i < end && i < list.size()); i++)
		for (int i = start; (i < end); i++)
			list.remove(start);
			
	}

	/**
	 * Creates a new list from a range of elements of a given list.
	 * @param <E> the type of the list's element.
	 * @param list the given list.
	 * @param start the start index (inclusive) to be included in the resulting list.
	 * @param end the end index (exclusive) to be included in the resulting list.
	 * @return a new list, containing all elements in the specified range of the given list.
	 */
	@SuppressWarnings("unchecked") 
	public static <E> List<E> copyItems(List<E> list, int start, int end) {
		// No way to determine the actual type of E at compile time to cast newInstance(), so @SuppressWarnings("unchecked") 
	
		if (list == null) return null;
		
		Logger localLogger = LoggerFactory.getLogger(ListUtil.class);
		List<E> res = null;
			try {
				res = list.getClass().newInstance();
				
			} catch (InstantiationException e) {
				
				localLogger.debug("List instantiation failed in ListUtil.copyItems(): " + e.getMessage());
				return null;
				
			} catch (IllegalAccessException e) {
				
				localLogger.debug("List instantiation failed in ListUtil.copyItems(): " + e.getMessage());
				return null;
			}
			
			if (Util.checkIndex(start, list) && Util.checkIndex(end, list))
				res.addAll(list.subList(start, end + 1));		
					
		return res;
	}
		
	/**
	 * Creates a new list and moves therein a range of elements from a given list. The given list won't contain
	 * the moved elements anymore.
	 * @param <E> the type of the list's element.
	 * @param buffer the given list.
	 * @param start the start index (inclusive) to be included in the resulting list.
	 * @param end the end index (exclusive) to be included in the resulting list.
	 * @return a new list, containing all elements in the specified range of the given list.
	 */
	public static <E> List<E> moveItems(List<E> buffer, int start, int end) {
	
		List<E> res = copyItems(buffer, start, end);
		if (res == null) return null;
		
		buffer.subList(start, end + 1).clear();
		
		return res;
	}
	
	/**
	 * Moves all items from a given list to a newly created list. The given list won't contain
	 * the moved elements anymore.
	 * @param <E> the type of the list's element.
	 * @param buffer the given list.
	 * @return the resulting list, containing all elements of the given list.
	 */
	public static <E> List<E> moveItems(List<E> buffer) {
		
		List<E> res = copyItems(buffer, 0, buffer.size() - 1);
		if (res == null) return null;
		
		buffer.clear();
		
		return res;
	}
	
	/**
	 * Gets a first non-null element of a given list.
	 * @param <E> the type of the list's element.
	 * @param list the given list.
	 * @return the found element or null if nothing found (all elements are null, or the list is empty).
	 */
	public static <E> E getFirstNonNullItem(List<E> list) {
		
		if (Util.isEmpty(list)) return null;

		for (E item : list)			
			if (item != null) return item;

		return null;		
	}

	/**
	 * Creates a new list of strings and fills it with the data read from a given resource.
	 * @param classRef reference to a class associated with the given resource.
	 * @param resourceLocation the name of resource.
	 * @return the new list loaded from the resource. 
	 */
	public static List<String> loadList(Class<?> classRef, String resourceLocation) {
		
		List<String> res = new ArrayList<String>();
		
		loadList(res, classRef, resourceLocation);		
		return res;
	}
	
	/**
	 * Fills in an existing list of strings with the data read from a given resource.
	 * @param list the given list of strings.
	 * @param classRef reference to a class associated with the given resource.
	 * @param resourceLocation the name of resource.
	 */
	public static void loadList(List<String> list, Class<?> classRef, String resourceLocation) {

		if (list == null) return;
		if (classRef == null) return;
		if (Util.isEmpty(resourceLocation)) return;
		
		BufferedReader reader = null;
		Logger localLogger = LoggerFactory.getLogger(ListUtil.class);
		
		try {
			reader = new BufferedReader(new InputStreamReader(classRef.getResourceAsStream(resourceLocation), "UTF-8"));
			
		} catch (UnsupportedEncodingException e) {
			localLogger.debug(String.format("ListUtil.loadList() encoding problem of \"%s\": %s", resourceLocation, e.getMessage()));
			return;
		}
		
		try {
			list.clear();
			
			while (reader.ready()) {
				
				String line = reader.readLine();
				if (line == null) break;
				if (Util.isEmpty(line)) continue;
				
				if (!line.startsWith("#"))
					list.add(line);
			}
		} catch (IOException e) {
			
			localLogger.debug(String.format("ListUtil.loadList() IO problem of \"%s\": %s", resourceLocation, e.getMessage()));
			return;
		}	
	}
	
}
