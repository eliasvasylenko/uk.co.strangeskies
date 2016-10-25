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
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterize;
import static uk.co.strangeskies.reflection.test.matchers.IsAssignableTo.isAssignableTo;

import java.util.Comparator;
import java.util.Set;

import org.junit.Test;

@SuppressWarnings({ "rawtypes", "javadoc" })
public class AssignabilityTest {
	interface StringComparator extends Comparator<String> {}

	interface RawComparator extends Comparator {}

	@Test
	public void classToClassAssignabilityTest() {
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
	public void classWithParameterizedSuperclassToParameterizedAssignabilityTest() {
		assertThat(StringComparator.class, isAssignableTo(parameterize(Comparator.class, String.class)));
		assertThat(StringComparator.class, not(isAssignableTo(parameterize(Comparator.class, Number.class))));
	}

	@Test
	public void classWithParameterizedSuperclassToRawAssignabilityTest() {
		assertThat(StringComparator.class, isAssignableTo(Comparator.class));
	}

	@Test
	public void rawAndParameterizedContainmentTest() {
		assertThat(parameterize(Set.class, Set.class),
				not(isAssignableTo(parameterize(Set.class, parameterize(Set.class, String.class)))));

		assertThat(parameterize(Set.class, parameterize(Set.class, String.class)),
				not(isAssignableTo(parameterize(Set.class, Set.class))));
	}
}
