/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.text.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties.test;

import static org.junit.Assert.assertEquals;
import static uk.co.strangeskies.text.properties.LocaleManager.getManager;

import org.junit.Test;

import uk.co.strangeskies.text.properties.LocaleManager;
import uk.co.strangeskies.text.properties.PropertyLoader;

@SuppressWarnings("javadoc")
public class PropertiesTest {
	public TestProperties text(LocaleManager manager) {
		return PropertyLoader.getPropertyLoader(manager).getProperties(TestProperties.class);
	}

	@Test
	public void managerTest() {
		getManager();
	}

	@Test
	public void propertiesTest() {
		text(getManager());
	}

	@Test
	public void simpleTextTest() {
		TestProperties text = text(getManager());

		assertEquals("simple property value", text.simple().toString());
	}

	@Test
	public void substitutionTextTest() {
		TestProperties text = text(getManager());

		assertEquals("value of substitution", text.substitution("substitution").toString());
	}

	@Test
	public void multipleSubstitutionTextTest() {
		TestProperties text = text(getManager());

		assertEquals("values of substitution one and substitution two",
				text.multipleSubstitution("substitution one", "substitution two").toString());
	}

	@Test
	public void defaultTextTest() {
		TestProperties text = text(getManager());

		assertEquals("value of default", text.defaultMethod().toString());
	}

	@Test
	public void copyTextTest() {
		TestProperties text = text(getManager());

		assertEquals("simple property value", text.copy().simple().toString());
	}

	@Test
	public void nestedTextTest() {
		TestProperties text = text(getManager());

		assertEquals("nested text value", text.nesting().nestedText().toString());
	}

	@Test
	public void deeplyNestedTextTest() {
		TestProperties text = text(getManager());

		assertEquals("deeply nested text value", text.nesting().deeply().deeplyNestedText().toString());
	}
}
