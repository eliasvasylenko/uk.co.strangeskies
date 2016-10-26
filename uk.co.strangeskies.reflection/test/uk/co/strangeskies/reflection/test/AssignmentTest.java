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

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.co.strangeskies.reflection.ArrayTypes.fromComponentType;
import static uk.co.strangeskies.reflection.IntersectionType.intersectionOf;
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterize;
import static uk.co.strangeskies.reflection.TypeVariableCapture.captureWildcard;
import static uk.co.strangeskies.reflection.TypeVariables.unboundedTypeVariable;
import static uk.co.strangeskies.reflection.TypeVariables.upperBoundedTypeVariable;
import static uk.co.strangeskies.reflection.WildcardTypes.unboundedWildcard;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardSuper;
import static uk.co.strangeskies.reflection.test.matchers.IsAssignableTo.isAssignableTo;
import static uk.co.strangeskies.reflection.test.matchers.IsSubtypeOf.isSubtypeOf;

import java.io.Serializable;
import java.lang.reflect.TypeVariable;
import java.util.Set;

import org.junit.Test;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.TypeVariableCapture;

@SuppressWarnings({ "rawtypes", "javadoc" })
public class AssignmentTest {
	interface StringComparable extends Comparable<String> {}

	interface RawComparable extends Comparable {}

	@Test
	public void classToClassAssignmentTest() {
		assertThat(Object.class, isAssignableTo(Object.class));
		assertThat(String.class, isAssignableTo(String.class));
		assertThat(Object.class, not(isAssignableTo(String.class)));
		assertThat(String.class, isAssignableTo(Object.class));

		assertThat(Integer.class, isAssignableTo(Number.class));
		assertThat(Double.class, isAssignableTo(Number.class));
		assertThat(Number.class, not(isAssignableTo(Integer.class)));
		assertThat(Integer.class, not(isAssignableTo(Double.class)));
	}

	@Test
	public void classWithParameterizedSuperclassToParameterizedAssignmentTest() {
		assertThat(StringComparable.class, isAssignableTo(parameterize(Comparable.class, String.class)));
		assertThat(StringComparable.class, not(isAssignableTo(parameterize(Comparable.class, Number.class))));
	}

	@Test
	public void classWithParameterizedSuperclassToRawAssignmentTest() {
		assertThat(StringComparable.class, isAssignableTo(Comparable.class));
	}

	@Test
	public void classWithRawSupertypeToParameterizedAssignmentTest() {
		assertThat(RawComparable.class, not(isAssignableTo(parameterize(Comparable.class, Number.class))));
	}

	@Test
	public void classWithRawSupertypeToRawAssignmentTest() {
		assertThat(RawComparable.class, isAssignableTo(Comparable.class));
	}

	@Test
	public void rawAndParameterizedContainmentTest() {
		assertThat(parameterize(Set.class, Set.class),
				not(isAssignableTo(parameterize(Set.class, parameterize(Set.class, String.class)))));

		assertThat(parameterize(Set.class, parameterize(Set.class, String.class)),
				not(isAssignableTo(parameterize(Set.class, Set.class))));
	}

	@Test
	public void primitiveAndBoxAssignmentTest() {
		assertThat(int.class, isAssignableTo(Integer.class));
		assertThat(Integer.class, isAssignableTo(int.class));

		assertThat(fromComponentType(Integer.class), not(isAssignableTo(fromComponentType(int.class))));
		assertThat(fromComponentType(int.class), not(isAssignableTo(fromComponentType(Integer.class))));
	}

	@Test
	public void primitiveAndBoxSubtypeTest() {
		assertThat(int.class, not(isSubtypeOf(Integer.class)));
		assertThat(Integer.class, not(isSubtypeOf(int.class)));
	}

	@Test
	public void assignToIntersectionTypeTest() {
		assertThat(Integer.class, isAssignableTo(intersectionOf(Number.class, Comparable.class)));

		assertThat(Integer.class,
				isAssignableTo(intersectionOf(Number.class, parameterize(Comparable.class, unboundedWildcard()))));

		assertThat(Integer.class,
				not(isAssignableTo(intersectionOf(Number.class, parameterize(Comparable.class, Number.class)))));
	}

	@Test
	public void assignFromIntersectionTypeTest() {
		assertThat(intersectionOf(Comparable.class, Serializable.class), isAssignableTo(Comparable.class));

		assertThat(intersectionOf(Comparable.class, Serializable.class),
				isAssignableTo(parameterize(Comparable.class, unboundedWildcard())));
	}

	@Test
	public void subtypeOfIntersectionTypeTest() {
		assertThat(Integer.class, isSubtypeOf(intersectionOf(Number.class, Comparable.class)));

		assertThat(Integer.class,
				isSubtypeOf(intersectionOf(Number.class, parameterize(Comparable.class, unboundedWildcard()))));

		assertThat(Integer.class,
				not(isSubtypeOf(intersectionOf(Number.class, parameterize(Comparable.class, Number.class)))));
	}

	@Test
	public void supertypeOfIntersectionTypeTest() {
		assertThat(intersectionOf(Comparable.class, Serializable.class), isSubtypeOf(Comparable.class));

		assertThat(intersectionOf(Comparable.class, Serializable.class),
				not(isSubtypeOf(parameterize(Comparable.class, unboundedWildcard()))));
	}

	@Test
	public void assignmentToWildcardTest() {
		assertThat(Object.class, not(isAssignableTo(unboundedWildcard())));
		assertThat(Integer.class, not(isAssignableTo(unboundedWildcard())));

		assertThat(Integer.class, isAssignableTo(wildcardSuper(Number.class)));
		assertThat(Integer.class, isAssignableTo(wildcardSuper(Comparable.class)));
		assertThat(Integer.class, isAssignableTo(wildcardSuper(parameterize(Comparable.class, Integer.class))));

		assertThat(Integer.class, not(isAssignableTo(wildcardExtending(Integer.class))));
	}

	@Test
	public void assignmentFromWildcardTest() {
		assertThat(unboundedWildcard(), isAssignableTo(Object.class));
		assertThat(unboundedWildcard(), not(isAssignableTo(Integer.class)));

		assertThat(wildcardSuper(Number.class), isAssignableTo(Object.class));
		assertThat(wildcardSuper(Integer.class), not(isAssignableTo(Number.class)));

		assertThat(wildcardExtending(Number.class), isAssignableTo(Object.class));
		assertThat(wildcardExtending(Integer.class), isAssignableTo(Number.class));
		assertThat(wildcardExtending(Integer.class), isAssignableTo(Comparable.class));
		assertThat(wildcardExtending(Integer.class), isAssignableTo(parameterize(Comparable.class, Integer.class)));
	}

	@Test
	public void assignToTypeVariableCaptureTest() {
		assertThat(Object.class, not(isAssignableTo(captureWildcard(unboundedWildcard()))));
		assertThat(Integer.class, not(isAssignableTo(captureWildcard(unboundedWildcard()))));

		assertThat(Integer.class, isAssignableTo(captureWildcard(wildcardSuper(Number.class))));
		assertThat(Integer.class, isAssignableTo(captureWildcard(wildcardSuper(Comparable.class))));
		assertThat(Integer.class,
				isAssignableTo(captureWildcard(wildcardSuper(parameterize(Comparable.class, Integer.class)))));

		assertThat(Integer.class, not(isAssignableTo(captureWildcard(wildcardExtending(Integer.class)))));
	}

	@Test
	public void assignFromTypeVariableCaptureTest() {
		assertThat(captureWildcard(unboundedWildcard()), isAssignableTo(Object.class));
		assertThat(captureWildcard(unboundedWildcard()), not(isAssignableTo(Integer.class)));

		assertThat(captureWildcard(wildcardSuper(Number.class)), isAssignableTo(Object.class));
		assertThat(captureWildcard(wildcardSuper(Integer.class)), not(isAssignableTo(Number.class)));

		assertThat(captureWildcard(wildcardExtending(Number.class)), isAssignableTo(Object.class));
		assertThat(captureWildcard(wildcardExtending(Integer.class)), isAssignableTo(Number.class));
		assertThat(captureWildcard(wildcardExtending(Integer.class)), isAssignableTo(Comparable.class));
		assertThat(captureWildcard(wildcardExtending(Integer.class)),
				isAssignableTo(parameterize(Comparable.class, Integer.class)));

		TypeVariableCapture type = captureWildcard(unboundedWildcard());
		assertThat(type, isAssignableTo(captureWildcard(wildcardSuper(type))));
		assertThat(captureWildcard(wildcardExtending(type)), isAssignableTo(type));
		assertThat(captureWildcard(wildcardExtending(type)), isAssignableTo(captureWildcard(wildcardSuper(type))));
	}

	@Test
	public void assignFromTypeVariableTest() {
		TypeVariable<?> unbounded = unboundedTypeVariable(null, "");

		assertThat(unbounded, isAssignableTo(Object.class));
		assertThat(unbounded, not(isAssignableTo(Integer.class)));

		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(Number.class))),
				isAssignableTo(Object.class));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(Integer.class))),
				isAssignableTo(Number.class));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(Integer.class))),
				isAssignableTo(Comparable.class));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(Integer.class))),
				isAssignableTo(parameterize(Comparable.class, Integer.class)));

		assertThat(unbounded, isAssignableTo(captureWildcard(wildcardSuper(unbounded))));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(unbounded))),
				isAssignableTo(unbounded));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(unbounded))),
				isAssignableTo(captureWildcard(wildcardSuper(unbounded))));
	}

	@Test
	public void assignToArrayTypeTest() {
		assertThat(String.class, not(isAssignableTo(fromComponentType(String.class))));
	}

	@Test
	public void assignFromArrayTypeTest() {
		assertThat(fromComponentType(String.class), not(isAssignableTo(String.class)));
	}

	@Test
	public void assignToGenericArrayTypeTest() {
		assertThat(parameterize(Comparable.class, String.class),
				not(isAssignableTo(fromComponentType(parameterize(Comparable.class, String.class)))));

		assertThat(fromComponentType(StringComparable.class),
				isAssignableTo(fromComponentType(parameterize(Comparable.class, String.class))));
	}

	@Test
	public void assignFromGenericArrayTypeTest() {
		assertThat(fromComponentType(parameterize(Comparable.class, String.class)),
				not(isAssignableTo(parameterize(Comparable.class, String.class))));

		assertThat(fromComponentType(parameterize(Comparable.class, String.class)), not(isAssignableTo(Comparable.class)));

		assertThat(fromComponentType(Comparable.class),
				isAssignableTo(fromComponentType(parameterize(Comparable.class, String.class))));

		assertThat(fromComponentType(parameterize(Comparable.class, String.class)),
				isAssignableTo(fromComponentType(Comparable.class)));

		assertThat(fromComponentType(parameterize(Comparable.class, String.class)),
				not(isAssignableTo(fromComponentType(String.class))));

		assertThat(fromComponentType(parameterize(Comparable.class, Number.class)),
				isAssignableTo(fromComponentType(parameterize(Comparable.class, wildcardSuper(Integer.class)))));

		assertThat(fromComponentType(parameterize(Comparable.class, Number.class)),
				not(isAssignableTo(fromComponentType(parameterize(Comparable.class, Integer.class)))));
	}
}
