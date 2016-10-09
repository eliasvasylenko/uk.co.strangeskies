/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,L__   __||  _ `.        / \     |  \   | |  ,-`__`]  ,-`__`]
 *   ( (_`-`   | |   | | ) |       / . \    | . \  | | / .`  `  / .`  `
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `-`.   / /   \ \  | | \ \| || |   | || +--J
 *  \ \__.` /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,-
 *   `.__.-`   L_|   L_|    L_|/_/       \_\L_|   \__|  `-.__.'  `-.__.]
 *                   __    _         _      __      __
 *                 ,`_ `, | |   _   | |  ,-`__`]  ,`_ `,
 *                ( (_`-` | '-.) |  | | / .`  `  ( (_`-`
 *                 `._ `. | +-. <   | || '--.     `._ `.
 *                _   `. \| |  `-`. | || +--J    _   `. \
 *               \ \__.` /| |    \ \| | \ `.__,-\ \__.` /
 *                `.__.-` L_|    L_|L_|  `-.__.] `.__.-`
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

import java.io.Serializable;
import java.lang.reflect.AnnotatedType;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.AnnotatedTypeSubstitution;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Capture;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.reflection.TypeToken.Preserve;

@SuppressWarnings("javadoc")
public class AnnotatedTypeSubstitutionTest {
	@Test
	public <T extends Number> void noSubstitutionIdentityTest() {
		AnnotatedType type = new TypeToken<Map<@Capture ? extends Number[] @Preserve [], Map<? super @Infer Number, @Infer T>>>() {}
				.getAnnotatedDeclaration();

		AnnotatedType substitution = new AnnotatedTypeSubstitution().where(t -> false, t -> t).resolve(type);

		Assert.assertTrue(type == substitution);
	}

	@Test
	public <T extends Number> void doublyNestedWildcardSubstitutionTest() {
		AnnotatedType type = new TypeToken<Map<@Capture ? extends Comparable<?>[] @Preserve [], Map<? super @Infer Number, @Infer T>>>() {}
				.getAnnotatedDeclaration();

		AnnotatedType expected = new TypeToken<Map<@Capture ? extends Comparable<?>[] @Preserve [], Map<? super Serializable, @Infer T>>>() {}
				.getAnnotatedDeclaration();

		AnnotatedType substitution = new AnnotatedTypeSubstitution()
				.where(t -> t.getType().equals(Number.class), t -> AnnotatedTypes.over(Serializable.class)).resolve(type);

		Assert.assertEquals(expected.getType(), substitution.getType());
	}
}
