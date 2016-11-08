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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.codegen.ParameterizedDeclaration;
import uk.co.strangeskies.reflection.codegen.ParameterizedDefinition;
import uk.co.strangeskies.reflection.codegen.DeclaredTypeVariable;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.utilities.Self;

@SuppressWarnings("javadoc")
public class GenericSignatureTest {
	@Test
	public void noParametersSignatureTest() {
		Assert.assertEquals(Collections.emptyList(),
				new ParameterizedDefinition<>(new ParameterizedDeclaration()).getTypeVariables());
	}

	@Test
	public void unboundedParameterSignatureTest() {
		ParameterizedDeclaration signature = new ParameterizedDeclaration().withTypeVariable();

		List<? extends TypeVariable<?>> typeVariables = new ParameterizedDefinition<>(signature).getTypeVariables();

		Assert.assertEquals(1, typeVariables.size());
	}

	@Test
	public void parameterNamesTest() {
		ParameterizedDeclaration signature = new ParameterizedDeclaration()
				.withTypeVariable()
				.withTypeVariable()
				.withTypeVariable();

		List<? extends TypeVariable<?>> typeVariables = new ParameterizedDefinition<>(signature).getTypeVariables();

		Assert.assertEquals(Arrays.asList("T0", "T1", "T2"),
				typeVariables.stream().map(t -> t.getName()).collect(Collectors.toList()));
	}

	@Test
	public void selfBoundingTypeVariableTest() {
		ParameterizedDeclaration signature = new ParameterizedDeclaration();

		DeclaredTypeVariable typeVariableSignature = signature.addTypeVariable();
		typeVariableSignature.withUpperBounds(ParameterizedTypes.parameterizeUnchecked(Self.class, typeVariableSignature));

		TypeVariable<?> typeVariable = new ParameterizedDefinition<>(signature).getTypeParameters()[0];

		Type[] expectedBounds = new Type[] { ParameterizedTypes.parameterizeUnchecked(Self.class, typeVariable) };
		Type[] bounds = typeVariable.getBounds();
		Assert.assertArrayEquals(expectedBounds, bounds);
	}

	@Test(expected = ReflectionException.class)
	public void invalidBoundsTest() {
		ParameterizedDeclaration signature = new ParameterizedDeclaration()
				.withTypeVariable(new TypeToken<Set<String>>() {}, new TypeToken<Set<Number>>() {});

		new ParameterizedDefinition<>(signature);
	}
}
