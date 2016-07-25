/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.utilities.test;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.utilities.Enumeration;

/**
 * Careful design is necessary here, as in some cases we are testing what
 * happens during class initialisation, so extra thought must be put into test
 * isolation
 * 
 * @author Elias N Vasylenko
 *
 */
public class EnumerationTest {
	/**
	 * Confirm that {@link Enumeration}s are properly initialised when accessed
	 * first by literal, rather than via {@link Enumeration#getConstants(Class)}.
	 * This is significant because of the odd initialisation logic to enforce
	 * instantiation only inside static initialisers.
	 */
	@Test
	public void testEnumLiteralAccess() {
		Assert.assertEquals("first", NamedEnum.FIRST.name());
		Assert.assertEquals("second", NamedEnum.SECOND.name());
		Assert.assertEquals("third", NamedEnum.THIRD.name());
	}

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
				.assertEquals(4, Enumeration.getConstants(PopulatedEnum.class).size());
	}

	/**
	 * Confirm that the members of {@link Enumeration} are available in
	 * instantiated order.
	 */
	@Test
	public void testEnumMemberOrder() {
		for (int i = 0; i < Enumeration.getConstants(NumberedEnum.class).size(); i++) {
			Assert.assertEquals(i, Enumeration.getConstants(NumberedEnum.class)
					.get(i).getInstance());
		}
	}

	/**
	 * Confirm that attempts to instantiate instances of an Enumeration after the
	 * static initialiser has completed throw the appropriate exception.
	 */
	@Test(expected = IllegalStateException.class)
	public void testPostStaticInitialiserFailure() {
		class EmptyEnum extends Enumeration<EmptyEnum> {
			public EmptyEnum() {
				super("");
			}
		}

		new EmptyEnum();
	}

	/**
	 * Confirm that attempts to instantiate instances of an Enumeration after the
	 * static initialiser has completed do not add a constant in an incomplete
	 * state.
	 */
	@Test
	public void testPostStaticInitialiserImmutability() {
		class EmptyEnum extends Enumeration<EmptyEnum> {
			public EmptyEnum() {
				super("");
			}
		}

		try {
			new EmptyEnum();
		} catch (IllegalStateException e) {}
		Assert.assertEquals(0, Enumeration.getConstants(EmptyEnum.class).size());
	}

	/**
	 * Confirm that attempts to instantiate multiple instances of an Enumeration
	 * with the same name will fail.
	 */
	@Test(expected = ExceptionInInitializerError.class)
	public void testUniqueNames() {
		Enumeration.getConstants(UniqueEnum.class);
	}

	static class InnerEnum extends Enumeration<InnerEnum> {
		public static final InnerEnum ELEMENT = new InnerEnum("test");

		public InnerEnum(String name) {
			super(name);
		}
	}
}

class UniqueEnum extends Enumeration<UniqueEnum> {
	public static final UniqueEnum FIRST = new UniqueEnum();
	public static final UniqueEnum SECOND = new UniqueEnum();

	public UniqueEnum() {
		super("Unique");
	}
}

class PopulatedEnum extends Enumeration<PopulatedEnum> {
	private static int counter = 0;

	public static final PopulatedEnum INSTANCE0 = new PopulatedEnum();
	public static final PopulatedEnum INSTANCE1 = new PopulatedEnum();
	public static final PopulatedEnum INSTANCE2 = new PopulatedEnum();
	public static final PopulatedEnum INSTANCE3 = new PopulatedEnum();

	private PopulatedEnum() {
		super("Instance" + counter++);
	}
}

class NumberedEnum extends Enumeration<NumberedEnum> {
	private static int counter = 0;

	public static final NumberedEnum INSTANCE0 = new NumberedEnum();
	public static final NumberedEnum INSTANCE1 = new NumberedEnum();
	public static final NumberedEnum INSTANCE2 = new NumberedEnum();

	private final int instance;

	private NumberedEnum() {
		super(Objects.toString(counter));

		this.instance = counter++;
	}

	public int getInstance() {
		return instance;
	}
}

class NamedEnum extends Enumeration<NamedEnum> {
	public static final NamedEnum FIRST = new NamedEnum("first");
	public static final NamedEnum SECOND = new NamedEnum("second");
	public static final NamedEnum THIRD = new NamedEnum("third");

	private NamedEnum(String name) {
		super(name);
	}
}
