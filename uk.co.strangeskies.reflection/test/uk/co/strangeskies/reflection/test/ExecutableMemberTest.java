/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.test;

import static uk.co.strangeskies.reflection.InvocableMember.resolveMethodOverload;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.InvocableMember;
import uk.co.strangeskies.reflection.TypeToken;

@SuppressWarnings("javadoc")
public class ExecutableMemberTest {
	@Test
	public void emptyVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		InvocableMember<?, ?> asList = InvocableMember.over(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		Object list = asList.invoke(null);

		Assert.assertEquals(Collections.emptyList(), list);
	}

	@Test
	public void singleVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		InvocableMember<?, ?> asList = InvocableMember.over(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		Object list = asList.invoke(null, "");

		Assert.assertEquals(Arrays.asList(""), list);
	}

	@Test
	public void varargsInvocationTest() throws NoSuchMethodException, SecurityException {
		InvocableMember<?, ?> asList = InvocableMember.over(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		Object list = asList.invoke(null, "A", "B", "C");

		Assert.assertEquals(Arrays.asList("A", "B", "C"), list);
	}

	@Test
	public void emptyVarargsResolutionTest() throws NoSuchMethodException, SecurityException {
		InvocableMember<?, ?> asList = resolveMethodOverload(new TypeToken<Arrays>() {}, "asList");

		Assert.assertTrue(asList.isVariableArityInvocation());
	}

	@Test
	public void singleVarargsResolutionTest() throws NoSuchMethodException, SecurityException {
		InvocableMember<?, ?> asList = resolveMethodOverload(new TypeToken<Arrays>() {}, "asList", String.class);

		Assert.assertTrue(asList.isVariableArityInvocation());
	}

	@Test
	public void varargsResolutionTest() throws NoSuchMethodException, SecurityException {
		InvocableMember<?, ?> asList = resolveMethodOverload(new TypeToken<Arrays>() {}, "asList", String.class,
				String.class, String.class);

		Assert.assertTrue(asList.isVariableArityInvocation());
	}

	@Test
	public void varargsDefinitionTest() throws NoSuchMethodException, SecurityException {
		InvocableMember<?, ?> asList = InvocableMember.over(Arrays.class.getMethod("asList", Object[].class));

		Object list = asList.invoke(null, new Object[] { new Object[] { "A", "B", "C" } });

		Assert.assertEquals(Arrays.asList("A", "B", "C"), list);
	}

	@Test
	public void varargsDefinitionResolutionTest() throws NoSuchMethodException, SecurityException {
		InvocableMember<?, ?> asList = resolveMethodOverload(new TypeToken<Arrays>() {}, "asList",
				new TypeToken<String[]>() {});

		Assert.assertFalse(asList.isVariableArityInvocation());
	}
}
