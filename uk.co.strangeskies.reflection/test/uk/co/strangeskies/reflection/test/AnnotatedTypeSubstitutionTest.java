/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.AnnotatedArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.AnnotatedParameterizedTypes.parameterize;
import static uk.co.strangeskies.reflection.AnnotatedTypes.annotated;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcardSuper;
import static uk.co.strangeskies.reflection.Annotations.from;
import static uk.co.strangeskies.reflection.TypeVariables.typeVariableExtending;

import java.io.Serializable;
import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.AnnotatedTypeSubstitution;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.token.TypeToken.Capture;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.reflection.token.TypeToken.Retain;

@SuppressWarnings("javadoc")
public class AnnotatedTypeSubstitutionTest {
	private AnnotatedType createTestType(AnnotatedType type) {
		return parameterize(
				annotated(Map.class),
				wildcardExtending(
						asList(from(Capture.class)),
						arrayFromComponent(arrayFromComponent(annotated(Number.class), from(Retain.class)))),
				parameterize(
						annotated(Map.class),
						wildcardSuper(type),
						annotated(typeVariableExtending(Collection.class, "E"), from(Infer.class))));
	}

	@Test
	public <T extends Number> void noSubstitutionIdentityTest() {
		AnnotatedType type = createTestType(annotated(Number.class, from(Infer.class)));

		AnnotatedType substitution = new AnnotatedTypeSubstitution().where(t -> false, t -> t).resolve(type);

		Assert.assertTrue(type == substitution);
	}

	@Test
	public <T extends Number> void doublyNestedWildcardSubstitutionTest() {
		AnnotatedType type = createTestType(annotated(Number.class, from(Infer.class)));

		AnnotatedType expected = createTestType(annotated(Serializable.class));

		AnnotatedType substitution = new AnnotatedTypeSubstitution()
				.where(t -> t.getType().equals(Number.class), t -> AnnotatedTypes.annotated(Serializable.class))
				.resolve(type);

		Assert.assertEquals(expected.getType(), substitution.getType());
	}
}
