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
import uk.co.strangeskies.utilities.classpath.Classpath.EntryAttribute;
import uk.co.strangeskies.utilities.classpath.Classpath.PropertyType;

/**
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Theories.class)
public class ManifestAttributeEntryParsingTest {
	public static class AttributeEntryParsingTheory implements EntryAttribute {
		private final String name;
		private final PropertyType type;
		private final Object value;
		private final String parses;

		private EntryAttribute parsed;

		public AttributeEntryParsingTheory(String parses, String name, PropertyType type, Object value) {
			this.name = name;
			this.type = type;
			this.value = value;

			this.parses = parses;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public PropertyType type() {
			return type;
		}

		@Override
		public Object value() {
			return value;
		}

		public EntryAttribute parse() {
			if (parsed == null) {
				parsed = Classpath.parseManifestEntryAttribute(parses);
			}
			return parsed;
		}
	}

	private static AttributeEntryParsingTheory newTheory(String parses, String name, PropertyType type, Object value) {
		return new AttributeEntryParsingTheory(parses, name, type, value);
	}

	@DataPoint
	public static final AttributeEntryParsingTheory simpleString = newTheory("simpleString=test", "simpleString",
			PropertyType.STRING, "test");

	// @DataPoint
	// public static final AttributeEntryParsingTheory typedString =
	// newTheory("typedString", PropertyType.STRING, "test",
	// "typedString:String=test");

	// @DataPoint
	// public static final AttributeEntryParsingTheory quotedString =
	// newTheory("quotedString", PropertyType.STRING, "test",
	// "quotedString:String=\"test\"");

	// @DataPoint
	// public static final AttributeEntryParsingTheory escapedQuoteString =
	// newTheory("escapedQuoteString",
	// PropertyType.STRING, "te\"st", "escapedQuoteString:String=\"te\\\"st\"");

	@Theory
	public void testManifestEntryAttributeParserValid(AttributeEntryParsingTheory theory) {
		Assert.assertNotNull(theory.parse());
	}

	@Theory
	public void testManifestEntryAttributeNameParser(AttributeEntryParsingTheory theory) {
		Assert.assertNotNull(theory.parse().name());
		Assert.assertEquals(theory.name(), theory.parse().name());
	}

	@Theory
	public void testManifestEntryAttributeTypeParser(AttributeEntryParsingTheory theory) {
		Assert.assertNotNull(theory.parse().type());
		Assert.assertEquals(theory.type(), theory.parse().type());
	}

	@Theory
	public void testManifestEntryAttributeValueParser(AttributeEntryParsingTheory theory) {
		Assert.assertNotNull(theory.parse().value());
		Assert.assertEquals(theory.value(), theory.parse().value());
	}
}
