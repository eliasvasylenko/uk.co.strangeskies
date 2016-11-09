/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.codegen.test;

import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterizeUnchecked;
import static uk.co.strangeskies.reflection.codegen.TypeVariableDeclaration.declareTypeVariable;
import static uk.co.strangeskies.reflection.codegen.TypeVariableDeclaration.referenceTypeVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.codegen.ParameterizedDeclaration;
import uk.co.strangeskies.reflection.codegen.ParameterizedDefinition;
import uk.co.strangeskies.reflection.codegen.TypeVariableDeclaration;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utilities.Self;

@SuppressWarnings("javadoc")
public class GenericSignatureTest {
	static class ParameterizedDeclarationImpl extends ParameterizedDeclaration<ParameterizedDeclarationImpl> {
		public ParameterizedDeclarationImpl() {}

		private ParameterizedDeclarationImpl(
				Collection<? extends Annotation> annotations,
				Collection<? extends TypeVariableDeclaration> typeVariables) {
			super(annotations, typeVariables);
		}

		@Override
		protected ParameterizedDeclarationImpl withMethodDeclarationData(
				Collection<? extends Annotation> annotations,
				Collection<? extends TypeVariableDeclaration> typeVariables) {
			return new ParameterizedDeclarationImpl(annotations, typeVariables);
		}
	}

	@Test
	public void noParametersSignatureTest() {
		Assert.assertEquals(Collections.emptyList(),
				new ParameterizedDefinition<>(new ParameterizedDeclarationImpl()).getTypeVariables());
	}

	@Test
	public void unboundedParameterSignatureTest() {
		ParameterizedDeclaration<?> signature = new ParameterizedDeclarationImpl().withTypeVariables("A");

		Stream<? extends TypeVariable<?>> typeVariables = new ParameterizedDefinition<>(signature).getTypeVariables();

		Assert.assertEquals(1, typeVariables.count());
	}

	@Test
	public void parameterNamesTest() {
		ParameterizedDeclaration<?> signature = new ParameterizedDeclarationImpl().withTypeVariables("A", "B", "C");

		Stream<? extends TypeVariable<?>> typeVariables = new ParameterizedDefinition<>(signature).getTypeVariables();

		Assert.assertEquals(Arrays.asList("A", "B", "C"), typeVariables.map(t -> t.getName()).collect(Collectors.toList()));
	}

	@Test
	public void selfBoundingTypeVariableTest() {
		ParameterizedDeclaration<?> signature = new ParameterizedDeclarationImpl().withTypeVariables(
				declareTypeVariable("A").withBounds(parameterizeUnchecked(Self.class, referenceTypeVariable("A"))));

		TypeVariable<?> typeVariable = new ParameterizedDefinition<>(signature).getTypeParameters()[0];

		Type[] expectedBounds = new Type[] { ParameterizedTypes.parameterizeUnchecked(Self.class, typeVariable) };
		Type[] bounds = typeVariable.getBounds();
		Assert.assertArrayEquals(expectedBounds, bounds);
	}

	@Test(expected = ReflectionException.class)
	public void invalidBoundsTest() {
		ParameterizedDeclaration<?> signature = new ParameterizedDeclarationImpl().withTypeVariables(
				declareTypeVariable("A").withBounds(new TypeToken<Set<String>>() {}, new TypeToken<Set<Number>>() {}));

		new ParameterizedDefinition<>(signature);
	}
}
