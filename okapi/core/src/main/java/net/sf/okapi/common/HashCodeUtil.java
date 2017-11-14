/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>.
 * 
 * Example use case:
 * 
 * <pre>
 * public int hashCode() {
 * 	int result = HashCodeUtil.SEED;
 * 	//collect the contributions of various fields
 * 	result = HashCodeUtil.hash(result, fPrimitive);
 * 	result = HashCodeUtil.hash(result, fObject);
 * 	result = HashCodeUtil.hash(result, fArray);
 * 	return result;
 * }
 * </pre>
 */
public final class HashCodeUtil {

	/**
	 * An initial value for a <code>hashCode</code>, to which is added
	 * contributions from fields. Using a non-zero value decreases collisions of
	 * <code>hashCode</code> values.
	 */
	public static final int SEED = 23;

	/**
	 * @param aSeed initial value of the <code>hashCode</code>
	 * @param aBoolean value to contribute to the <code>hashCode</code>
	 * @return hash code for a boolean.
	 */
	public static int hash(int aSeed, boolean aBoolean) {	
		return firstTerm(aSeed) + (aBoolean ? 1 : 0);
	}

	/**
	 * @param aSeed initial value of the <code>hashCode</code>
	 * @param aChar value to contribute to the <code>hashCode</code>
	 * @return has code for a char value.
	 */
	public static int hash(int aSeed, char aChar) {		
		return firstTerm(aSeed) + (int) aChar;
	}

	/**
	 * @param aSeed initial value of the <code>hashCode</code>
	 * @param aInt value to contribute to the <code>hashCode</code>
	 * @return hash code for int.
	 */
	public static int hash(int aSeed, int aInt) {
		/*
		 * Implementation Note: byte and short are handled by this
		 * method, through implicit conversion.
		 */
		return firstTerm(aSeed) + aInt;
	}

	/**
	 * @param aSeed initial value of the <code>hashCode</code>
	 * @param aLong value to contribute to the <code>hashCode</code>
	 * @return hash code for a long value.
	 */
	public static int hash(int aSeed, long aLong) {
		return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
	}

	/**
	 * @param aSeed initial value of the <code>hashCode</code>
	 * @param aFloat value to contribute to the <code>hashCode</code>
	 * @return a hash code for a float value.
	 */
	public static int hash(int aSeed, float aFloat) {
		return hash(aSeed, Float.floatToIntBits(aFloat));
	}

	/**
	 * @param aSeed initial value of the <code>hashCode</code>
	 * @param aDouble value to contribute to the <code>hashCode</code>
	 * @return a hash code for a double value.
	 */
	public static int hash(int aSeed, double aDouble) {
		return hash(aSeed, Double.doubleToLongBits(aDouble));
	}

	/**
	 * <code>aObject</code> is a possibly-null object field, and possibly an
	 * array.
	 * 
	 * If <code>aObject</code> is an array, then each element may be a primitive
	 * or a possibly-null object.
	 *
	 * @param aSeed initial value of the <code>hashCode</code>
	 * @param aObject value to contribute to the <code>hashCode</code>
	 * @return a hash code for an {@link Object} ({@link Array}-aware).
	 */
	public static int hash(int aSeed, Object aObject) {
		int result = aSeed;
		if (aObject == null) {
			result = hash(result, 0);
		} else if (!isArray(aObject)) {
			result = hash(result, aObject.hashCode());
		} else {
			int length = Array.getLength(aObject);
			for (int idx = 0; idx < length; ++idx) {
				Object item = Array.get(aObject, idx);
				// recursive call!
				result = hash(result, item);
			}
		}
		return result;
	}

	// PRIVATE methods
	private static final int fODD_PRIME_NUMBER = 37;

	private static int firstTerm(int aSeed) {
		return fODD_PRIME_NUMBER * aSeed;
	}

	private static boolean isArray(Object aObject) {
		return aObject.getClass().isArray();
	}
}
