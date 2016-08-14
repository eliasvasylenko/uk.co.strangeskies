package uk.co.strangeskies.reflection.test;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ExecutableMember;
import uk.co.strangeskies.reflection.TypeToken;

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

	@Test
	public void emptyVarargsResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableMember<?, ?> asList = new TypeToken<Arrays>() {}.resolveMethodOverload("asList");

		Assert.assertTrue(asList.isVariableArityInvocation());
	}

	@Test
	public void singleVarargsResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableMember<?, ?> asList = new TypeToken<Arrays>() {}.resolveMethodOverload("asList", String.class);

		Assert.assertTrue(asList.isVariableArityInvocation());
	}

	@Test
	public void varargsResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableMember<?, ?> asList = new TypeToken<Arrays>() {}.resolveMethodOverload("asList", String.class,
				String.class, String.class);

		Assert.assertTrue(asList.isVariableArityInvocation());
	}

	@Test
	public void varargsDefinitionTest() throws NoSuchMethodException, SecurityException {
		ExecutableMember<?, ?> asList = ExecutableMember.over(Arrays.class.getMethod("asList", Object[].class));

		Object list = asList.invoke(null, new Object[] { new Object[] { "A", "B", "C" } });

		Assert.assertEquals(Arrays.asList("A", "B", "C"), list);
	}

	@Test
	public void varargsDefinitionResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableMember<?, ?> asList = new TypeToken<Arrays>() {}.resolveMethodOverload("asList",
				new TypeToken<String[]>() {});

		Assert.assertFalse(asList.isVariableArityInvocation());
	}
}
