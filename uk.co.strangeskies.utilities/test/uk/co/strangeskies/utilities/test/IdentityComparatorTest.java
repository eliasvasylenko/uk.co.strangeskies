/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.strangeskies.utilities.IdentityComparator;

/**
 * It is very difficult or impossible to test this class properly, as the
 * important aspects of its behaviour either only occur in extremely rare
 * circumstances, or change slightly with implementation details of the VM, or
 * both.
 * 
 * @author tofuser
 *
 */
public class IdentityComparatorTest {
	@Test
	public void compareIdenticalReferences() {
		IdentityComparator<Object> identityComparator = new IdentityComparator<>();

		Object reference = new Object();
		Assert.assertEquals(identityComparator.compare(reference, reference), 0);
	}

	@Test
	public void compareDifferentReferences() {
		IdentityComparator<Object> identityComparator = new IdentityComparator<>();
		Assert.assertNotEquals(
				identityComparator.compare(new Object(), new Object()), 0);
	}
}
