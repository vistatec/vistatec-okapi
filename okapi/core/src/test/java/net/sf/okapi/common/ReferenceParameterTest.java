package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReferenceParameterTest {

	@Test
	public void testDetectionAndAccess ()
		throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
	{
		StringParameters params = new StringParameters();
		Method[] methods = params.getClass().getMethods();
		for ( Method m : methods ) {
			if ( Modifier.isPublic(m.getModifiers() ) && m.isAnnotationPresent(ReferenceParameter.class)) {
				String data = (String)m.invoke(params);
				assertEquals("reference", data);
				// Test changing the value
				String getMethodName = m.getName();
				String setMethodName = "set"+getMethodName.substring(3);
				Method setMethod = params.getClass().getMethod(setMethodName, String.class);
				setMethod.invoke(params, "NewValue");
				data = (String)m.invoke(params);
				assertEquals("NewValue", data);
			}
		}
	}
	
}
