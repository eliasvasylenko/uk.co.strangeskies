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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.GenericDefinition;
import uk.co.strangeskies.reflection.GenericSignature;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.TypeVariableSignature;

public class GenericSignatureTest {
	@Test
	public void noParametersSignatureTest() {
		Assert.assertEquals(Collections.emptyList(), new GenericDefinition<>(new GenericSignature()).getTypeVariables());
	}

	@Test
	public void unboundedParameterSignatureTest() {
		GenericSignature signature = new GenericSignature().withTypeVariable();

		List<? extends TypeVariable<?>> typeVariables = new GenericDefinition<>(signature).getTypeVariables();

		Assert.assertEquals(1, typeVariables.size());
	}

	@Test
	public void parameterNamesTest() {
		GenericSignature signature = new GenericSignature().withTypeVariable().withTypeVariable().withTypeVariable();

		List<? extends TypeVariable<?>> typeVariables = new GenericDefinition<>(signature).getTypeVariables();

		Assert.assertEquals(Arrays.asList("T0", "T1", "T2"),
				typeVariables.stream().map(t -> t.getName()).collect(Collectors.toList()));
	}

	@Test
	public void selfBoundingTypeVariableTest() {
		GenericSignature signature = new GenericSignature();

		TypeVariableSignature typeVariableSignature = signature.addTypeVariable();
		typeVariableSignature.withUpperBounds(ParameterizedTypes.uncheckedFrom(Set.class, typeVariableSignature));

		TypeVariable<?> typeVariable = new GenericDefinition<>(signature).getTypeParameters()[0];

		Assert.assertEquals(typeVariable, ((ParameterizedType) typeVariable.getBounds()[0]).getActualTypeArguments()[0]);
	}
}
