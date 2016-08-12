package uk.co.strangeskies.reflection.test;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ExecutableMember;

@SuppressWarnings("javadoc")
public class ExecutableMemberTest {
	@Test
	public void emptyVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableMember<?, ?> asList = ExecutableMember.over(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		Object list = asList.invoke(null);

		Assert.assertEquals(Collections.emptyList(), list);
	}

	@Test
	public void singleVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableMember<?, ?> asList = ExecutableMember.over(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		Object list = asList.invoke(null, "");

		Assert.assertEquals(Arrays.asList(""), list);
	}

	@Test
	public void varargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableMember<?, ?> asList = ExecutableMember.over(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		Object list = asList.invoke(null, "A", "B", "C");

		Assert.assertEquals(Arrays.asList("A", "B", "C"), list);
	}
}
