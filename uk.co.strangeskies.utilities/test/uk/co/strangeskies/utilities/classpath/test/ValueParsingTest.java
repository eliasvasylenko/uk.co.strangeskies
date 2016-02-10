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
package uk.co.strangeskies.utilities.classpath.test;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import uk.co.strangeskies.utilities.classpath.Classpath;

/**
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Theories.class)
public class ValueParsingTest {
	public static class ValueTheory {
		private final String value;
		private final String parses;

		private String parsed;

		public ValueTheory(String parses, String value) {
			this.value = value;

			this.parses = parses;
		}

		public String value() {
			return value;
		}

		public String parse() {
			if (parsed == null) {
				parsed = Classpath.parseValueString(parses);
			}
			return parsed;
		}
	}

	private static ValueTheory newTheory(String parses, String value) {
		return new ValueTheory(parses, value);
	}

	@DataPoint
	public static final ValueTheory simpleString = newTheory("simpleString", "simpleString");

	@DataPoint
	public static final ValueTheory doubleQuotedString = newTheory("\"quotedString\"", "quotedString");

	@DataPoint
	public static final ValueTheory singleQuotedString = newTheory("'quotedString'", "quotedString");

	@DataPoint
	public static final ValueTheory doubleQuotedEscapedString = newTheory("\"quoted\\\"String\"", "quoted\"String");

	@DataPoint
	public static final ValueTheory singleQuotedEscapedString = newTheory("'quoted\\'String'", "quoted'String");

	@Theory
	public void testValueParserValid(ValueTheory theory) {
		Assert.assertNotNull(theory.parse());
	}

	@Theory
	public void testValueParser(ValueTheory theory) {
		Assert.assertEquals(theory.value(), theory.parse());
	}
}
