/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.search;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.junit.Assert;

/**
 * User: Christian Hargraves
 */
public class Helper {

    /*
     * Invoke private constructor by reflection purely for code-coverage 
     */
    public static Object genericTestConstructor(final Class<?> cls) throws Exception {
        //This is going to be the only constructor since it is for testing private constructors ... why have
        //more than one private constructor?
        final Constructor<?> c = cls.getDeclaredConstructors()[0];
        c.setAccessible(true);
        final Object n = c.newInstance((Object[])null);
        Assert.assertNotNull(n);
        return n;
    }

    public static void setPrivateMember(Object instance, String memberName, Object value) throws Exception{
        Field field = instance.getClass().getDeclaredField(memberName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    public static Object getPrivateMember(Object instance, String memberName) throws Exception {
        Field field = instance.getClass().getDeclaredField(memberName);
        field.setAccessible(true);
        return field.get(instance);
    }
}
