/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ExecutableToken;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.reflection.test.ExecutableTokenTest.Outer.Inner;

@SuppressWarnings("javadoc")
public class ExecutableTokenTest {
	static class Outer<T> {
		class Inner<U extends T> {
			public Inner(U parameter) {}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void constructorWithEnclosingTypeTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken.overConstructor((Constructor<Object>) Inner.class.getConstructors()[0],
				new TypeToken<Outer<Number>>() {}, new TypeToken<Outer<Number>.Inner<Integer>>() {});
	}

	@SuppressWarnings("unchecked")
	@Test(expected = ReflectionException.class)
	public void constructorWithWrongEnclosingTypeTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken.overConstructor((Constructor<Object>) Inner.class.getConstructors()[0],
				new TypeToken<Outer<Number>>() {}, new TypeToken<Outer<Integer>.Inner<Integer>>() {});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void constructorWithRawEnclosingTypeTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken.overConstructor((Constructor<Object>) Inner.class.getConstructors()[0],
				new TypeToken<Outer<Number>>() {}, new TypeToken<Outer.Inner>() {});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void constructorWithUninferredEnclosingTypeTest() throws NoSuchMethodException, SecurityException {
		TypeToken<Outer<?>.Inner<Integer>> tt = new @Infer TypeToken<@Infer Outer<@Infer ?>.Inner<Integer>>() {};
		System.out.println(tt);
		System.out.println(tt.getResolver().getBounds());

		ExecutableToken.overConstructor((Constructor<Object>) Inner.class.getConstructors()[0],
				new TypeToken<Outer<Number>>() {}, new @Infer TypeToken<@Infer Outer<@Infer ?>.Inner<Integer>>() {});
	}

	@Test
	public void emptyVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = ExecutableToken.overMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		Object list = asList.invoke(null);

		Assert.assertEquals(Collections.emptyList(), list);
	}

	@Test
	public void singleVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = ExecutableToken.overMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		Object list = asList.invoke(null, "");

		Assert.assertEquals(Arrays.asList(""), list);
	}

	@Test
	public void varargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = ExecutableToken.overMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		Object list = asList.invoke(null, "A", "B", "C");

		Assert.assertEquals(Arrays.asList("A", "B", "C"), list);
	}

	@Test
	public void emptyVarargsResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = new TypeToken<Arrays>() {}.getMethods().named("asList").resolveOverload();

		Assert.assertTrue(asList.isVariableArityInvocation());
	}

	@Test
	public void singleVarargsResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = new TypeToken<Arrays>() {}.getMethods().named("asList")
				.resolveOverload(String.class);

		Assert.assertTrue(asList.isVariableArityInvocation());
	}

	@Test
	public void varargsResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = new TypeToken<Arrays>() {}.getMethods().named("asList").resolveOverload(String.class,
				String.class, String.class);

		Assert.assertTrue(asList.isVariableArityInvocation());
	}

	@Test
	public void varargsDefinitionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = ExecutableToken.overMethod(Arrays.class.getMethod("asList", Object[].class));

		Object list = asList.invoke(null, new Object[] { new Object[] { "A", "B", "C" } });

		Assert.assertEquals(Arrays.asList("A", "B", "C"), list);
	}

	@Test
	public void varargsDefinitionResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = new TypeToken<Arrays>() {}.getMethods().named("asList")
				.resolveOverload(new TypeToken<String[]>() {});

		Assert.assertFalse(asList.isVariableArityInvocation());
	}
}
