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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ClassDefinition;
import uk.co.strangeskies.reflection.ClassDefinition.ClassSignature;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.TypeVariableSignature;
import uk.co.strangeskies.utilities.Self;

public class ClassDefinitionTest {
	private static final String TEST_CLASS_NAME = ClassDefinitionTest.class.getPackage().getName() + ".SelfSet";

	@Test
	public void selfBoundingSetTest() {
		ClassSignature<?> classSignature = ClassDefinition.declare(TEST_CLASS_NAME);
		TypeVariableSignature typeVariableSignature = classSignature.addTypeVariable();
		typeVariableSignature.withUpperBounds(ParameterizedTypes.uncheckedFrom(Self.class, typeVariableSignature));

		ClassDefinition<?> classDefinition = classSignature
				.withSuperType(ParameterizedTypes.uncheckedFrom(Set.class, typeVariableSignature)).define();

		TypeVariable<?> typeVariable = classDefinition.getTypeParameters()[0];

		Type[] expectedBounds = new Type[] { ParameterizedTypes.uncheckedFrom(Self.class, typeVariable) };
		Type[] bounds = typeVariable.getBounds();
		Assert.assertArrayEquals(expectedBounds, bounds);
	}
}
