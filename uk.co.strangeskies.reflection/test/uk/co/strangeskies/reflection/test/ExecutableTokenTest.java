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

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.co.strangeskies.reflection.ExecutableToken.overConstructor;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

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
		ExecutableToken<?, ?> constructor = overConstructor((Constructor<Object>) Inner.class.getConstructors()[0],
				new TypeToken<Outer<Number>>() {}, new @Infer TypeToken<Outer<?>.Inner<Integer>>() {});

		assertThat(constructor.getReturnType().resolve(), equalTo(new TypeToken<Outer<Number>.Inner<Integer>>() {}));
	}

	@Test
	public void emptyVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = ExecutableToken.overMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		List<?> list = (List<?>) asList.invoke(null);

		assertThat(list, empty());
	}

	@Test
	public void singleVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = ExecutableToken.overMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		List<?> list = (List<?>) asList.invoke(null, "");

		assertThat(list, contains(""));
	}

	@Test
	public void varargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = ExecutableToken.overMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		List<?> list = (List<?>) asList.invoke(null, "A", "B", "C");

		assertThat(list, contains("A", "B", "C"));
	}

	@Test
	public void emptyVarargsResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = new TypeToken<Arrays>() {}.getMethods().named("asList").resolveOverload();

		assertThat(asList.isVariableArityInvocation(), is(true));
	}

	@Test
	public void singleVarargsResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = new TypeToken<Arrays>() {}.getMethods().named("asList")
				.resolveOverload(String.class);

		assertThat(asList.isVariableArityInvocation(), is(true));
	}

	@Test
	public void varargsResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = new TypeToken<Arrays>() {}.getMethods().named("asList").resolveOverload(String.class,
				String.class, String.class);

		assertThat(asList.isVariableArityInvocation(), is(true));
	}

	@Test
	public void varargsDefinitionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = ExecutableToken.overMethod(Arrays.class.getMethod("asList", Object[].class));

		List<?> list = (List<?>) asList.invoke(null, new Object[] { new Object[] { "A", "B", "C" } });

		assertThat(list, contains("A", "B", "C"));
	}

	@Test
	public void varargsDefinitionResolutionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> asList = new TypeToken<Arrays>() {}.getMethods().named("asList")
				.resolveOverload(new TypeToken<String[]>() {});

		assertThat(asList.isVariableArityInvocation(), is(false));
	}
}
