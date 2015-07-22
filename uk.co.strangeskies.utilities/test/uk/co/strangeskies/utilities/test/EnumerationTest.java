/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import uk.co.strangeskies.utilities.Enumeration;

/**
 * It is very difficult or impossible to test this class properly, as the
 * important aspects of its behaviour either only occur in extremely rare
 * circumstances, or change slightly with implementation details of the VM, or
 * both.
 * 
 * @author Elias N Vasylenko
 *
 */
public class EnumerationTest {
	/**
	 * Confirm that an {@link Enumeration} works as an inner class (implementation
	 * may perform stack trace examination).
	 */
	@Test
	public void testInnerEnum() {
		Assert.assertEquals(1, Enumeration.getConstants(InnerEnum.class).size());
	}

	/**
	 * Confirm that an {@link Enumeration} contains the expected number of
	 * members.
	 */
	@Test
	public void testEnumMemberCount() {
		Assert
				.assertEquals(3, Enumeration.getConstants(PopulatedEnum.class).size());
	}

	/**
	 * Confirm that the members of {@link Enumeration} are available in
	 * instantiated order.
	 */
	@Test
	public void testEnumMemberOrder() {
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals(i,
					Enumeration.getConstants(PopulatedEnum.class).get(i).getInstance());
		}
	}

	/**
	 * Confirm that attempts to instantiate instances of an Enumeration after the
	 * static initialiser has completed throw the appropriate exception.
	 */
	@Test(expected = IllegalStateException.class)
	public void testPostStaticInitialiserFailure() {
		new EmptyEnum();
	}

	/**
	 * Confirm that attempts to instantiate instances of an Enumeration after the
	 * static initialiser has completed do not add a constant in an incomplete
	 * state.
	 */
	@Test
	public void testPostStaticInitialiserImmutability() {
		try {
			new EmptyEnum();
		} catch (IllegalStateException e) {}
		Assert.assertEquals(0, Enumeration.getConstants(EmptyEnum.class).size());
	}

	static class InnerEnum extends Enumeration<EmptyEnum> {
		public static final InnerEnum ELEMENT = new InnerEnum("test");

		public InnerEnum(String name) {
			super(name);
		}
	}
}

class EmptyEnum extends Enumeration<EmptyEnum> {
	public EmptyEnum() {
		super("");
	}
}

class PopulatedEnum extends Enumeration<PopulatedEnum> {
	private static int counter = 0;

	public static final PopulatedEnum INSTANCE0 = new PopulatedEnum();
	public static final PopulatedEnum INSTANCE1 = new PopulatedEnum();
	public static final PopulatedEnum INSTANCE2 = new PopulatedEnum();

	private final int instance;

	private PopulatedEnum() {
		super("Instance" + counter);

		this.instance = counter++;
	}

	public int getInstance() {
		return instance;
	}
}
