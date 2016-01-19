/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.test;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.utilities.IdentityProperty;

/**
 * It is very difficult or impossible to test this class properly, as the
 * important aspects of its behaviour either only occur in extremely rare
 * circumstances, or change slightly with implementation details of the VM, or
 * both.
 * 
 * @author Elias N Vasylenko
 *
 */
public class IdentityPropertyTest {
	/**
	 * Confirm default constructor initialises with null value
	 */
	@Test
	public void testNullInitialisation() {
		Assert.assertEquals(null, new IdentityProperty<>().get());
	}

	/**
	 * Confirm value constructor initialises properly
	 */
	@Test
	public void testValueInitialisation() {
		Assert.assertEquals("value", new IdentityProperty<>("value").get());
	}

	/**
	 * Confirm property setting works
	 */
	@Test
	public void testSet() {
		IdentityProperty<String> property = new IdentityProperty<>();
		property.set("set");
		Assert.assertEquals("set", property.get());
	}

	/**
	 * Confirm property setting works multiple times
	 */
	@Test
	public void testMultipleSet() {
		IdentityProperty<String> property = new IdentityProperty<>();
		property.set("set");
		property.set("again");
		Assert.assertEquals("again", property.get());
	}
}
