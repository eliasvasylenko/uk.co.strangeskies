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
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection.test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.utilities.collection.StreamUtilities;

@SuppressWarnings("javadoc")
public class StreamUtilitiesTest {
	interface A {}

	interface B extends A {}

	interface C extends B {}

	interface D extends B, C {}

	@Test
	public void flatMapSingleLevelTest() {
		Assert.assertEquals(Arrays.asList(B.class, A.class), StreamUtilities
				.<Class<?>>flatMapRecursive(B.class, c -> Stream.of(c.getInterfaces())).collect(Collectors.toList()));
	}

	@Test
	public void flatMapTwoLevelsTest() {
		Assert.assertEquals(Arrays.asList(C.class, B.class, A.class), StreamUtilities
				.<Class<?>>flatMapRecursive(C.class, c -> Stream.of(c.getInterfaces())).collect(Collectors.toList()));
	}

	@Test
	public void flatMapThreeLevelsRepeatsTest() {
		Assert.assertEquals(Arrays.asList(D.class, B.class, A.class, C.class, B.class, A.class), StreamUtilities
				.<Class<?>>flatMapRecursive(D.class, c -> Stream.of(c.getInterfaces())).collect(Collectors.toList()));
	}

	@Test
	public void flatMapThreeLevelsDistinctTest() {
		Assert.assertEquals(Arrays.asList(D.class, B.class, A.class, C.class), StreamUtilities
				.<Class<?>>flatMapRecursiveDistinct(D.class, c -> Stream.of(c.getInterfaces())).collect(Collectors.toList()));
	}
}
