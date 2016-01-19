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
package uk.co.strangeskies.utilities.text.test;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theory;

import uk.co.strangeskies.utilities.Enumeration;

/**
 * Careful design is necessary here, as in some cases we are testing what
 * happens during class initialisation, so extra thought must be put into test
 * isolation
 * 
 * @author Elias N Vasylenko
 *
 */
@SuppressWarnings("javadoc")
public class StringEscaperTest {
	@DataPoint
	public static String SENTENCE = "The small brown dog.";

	/**
	 * Confirm that {@link Enumeration}s are properly initialised when accessed
	 * first by literal, rather than via {@link Enumeration#getConstants(Class)}.
	 * This is significant because of the odd initialisation logic to enforce
	 * instantiation only inside static initialisers.
	 */
	@Theory
	public void testEnumLiteralAccess() {}

	/**
	 * Confirm that an {@link Enumeration} works as an inner class (implementation
	 * may perform stack trace examination).
	 */
	@Theory
	public void testInnerEnum() {}
}
