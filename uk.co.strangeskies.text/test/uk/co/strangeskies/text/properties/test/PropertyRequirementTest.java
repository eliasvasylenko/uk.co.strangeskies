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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.co.strangeskies.text.properties.LocaleManager.getManager;

import org.junit.Test;

import uk.co.strangeskies.text.properties.LocaleManager;
import uk.co.strangeskies.text.properties.PropertyLoader;
import uk.co.strangeskies.text.properties.PropertyLoaderException;

@SuppressWarnings("javadoc")
public class PropertyRequirementTest {
	public RequirementTestProperties text(LocaleManager manager) {
		return PropertyLoader.getPropertyLoader(manager).getProperties(RequirementTestProperties.class);
	}

	@Test(expected = PropertyLoaderException.class)
	public void immediateRequirementTest() {
		text(getManager()).immediateRequirements();
	}

	@Test(expected = PropertyLoaderException.class)
	public void requiredPropertyTest() {
		RequirementTestProperties text = text(getManager());

		text.requiredProperty();
	}

	@Test
	public void defaultingRequiredPropertyTest() {
		RequirementTestProperties text = text(getManager());

		assertNotNull(text.defaultingRequiredProperty());
	}

	@Test
	public void optionalPropertyTest() {
		RequirementTestProperties text = text(getManager());

		assertFalse(text.optionalProperty().isPresent());
	}

	@Test
	public void defaultingOptionalPropertyTest() {
		RequirementTestProperties text = text(getManager());

		assertTrue(text.optionalProperty().isPresent());
	}

	@Test
	public void immediateOptionalPropertyTest() {
		RequirementTestProperties text = text(getManager());

		assertFalse(text.immediateOptionalProperty().isPresent());
	}

	@Test
	public void immediateDefaultingPropertyTest() {
		RequirementTestProperties text = text(getManager());

		assertNotNull(text.immediateDefaultingProperty());
	}
}
