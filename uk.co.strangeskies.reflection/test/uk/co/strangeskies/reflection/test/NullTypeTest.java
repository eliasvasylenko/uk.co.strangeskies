package uk.co.strangeskies.reflection.test;

import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.Types;

@SuppressWarnings("javadoc")
public class NullTypeTest {
	@Test
	public void assignabilityFromNullTest() {
		Assert.assertTrue(Types.isAssignable((Type) null, String.class));

		Assert.assertTrue(Types.isAssignable((Type) null, new Type[] { String.class, Number.class }));
	}

	@Test
	public void assignabilityToNullTest() {
		Assert.assertTrue(Types.isAssignable(String.class, (Type) null));

		Assert.assertTrue(Types.isAssignable(new Type[] { String.class, Number.class }, (Type) null));
	}
}
