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

import uk.co.strangeskies.utilities.classpath.Attribute;
import uk.co.strangeskies.utilities.classpath.AttributeProperty;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;
import uk.co.strangeskies.utilities.classpath.PropertyType;

/**
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Theories.class)
public class AttributeParsingTest {
	public static class AttributeParsingTheory extends Attribute {
		private final String parses;

		private Attribute parsed;

		public AttributeParsingTheory(String parses, String name, AttributeProperty<?>... attributeProperties) {
			super(name, attributeProperties);

			this.parses = parses;
		}

		public Attribute parse() {
			if (parsed == null) {
				parsed = ManifestUtilities.parseAttribute(parses);
			}
			return parsed;
		}
	}

	private static AttributeParsingTheory newTheory(String parses, String name,
			AttributeProperty<?>... attributeProperties) {
		return new AttributeParsingTheory(parses, name, attributeProperties);
	}

	@DataPoint
	public static final AttributeParsingTheory SIMPLE_STRING = newTheory("simpleString", "simpleString");

	@DataPoint
	public static final AttributeParsingTheory TYPED_PROPERTY = newTheory("typedProperty;property1:String=test",
			"typedProperty", new AttributeProperty<>("property1", PropertyType.STRING, "test"));

	@DataPoint
	public static final AttributeParsingTheory TWO_TYPED_PROPERTIES = newTheory(
			"twoTypedProperties;typedString:String=test;typedString2:String=\"test2\"", "twoTypedProperties",
			new AttributeProperty<>("typedString", PropertyType.STRING, "test"),
			new AttributeProperty<>("typedString2", PropertyType.STRING, "test2"));

	@Theory
	public void testManifestEntryAttributeParserValid(AttributeParsingTheory theory) {
		Assert.assertNotNull(theory.parse());
	}

	@Theory
	public void testManifestEntryAttributeNameParser(AttributeParsingTheory theory) {
		Assert.assertNotNull(theory.parse().name());
		Assert.assertEquals(theory.name(), theory.parse().name());
	}
}
