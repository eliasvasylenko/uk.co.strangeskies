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

import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.co.strangeskies.text.properties.LocaleManager;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.PropertyLoader;
import uk.co.strangeskies.utilities.IdentityProperty;

@SuppressWarnings("javadoc")
public class PropertiesTest {
	private static Locale SYSTEM_LOCALE;

	@BeforeClass
	public static void setLocale() {
		SYSTEM_LOCALE = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
	}

	@AfterClass
	public static void unsetLocale() {
		Locale.setDefault(SYSTEM_LOCALE);
	}

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
	public void languageTest() {
		LocalizerTestProperties text = text(getManager(Locale.FRENCH)).localization();

		assertEquals("French simple property value", text.simpleLocalized().toString());
	}

	@Test
	public void languageDefaultTest() {
		LocalizerTestProperties text = text(getManager(Locale.FRENCH)).localization();

		assertEquals("another simple property value", text.anotherSimpleLocalized().toString());
	}

	@Test
	public void languageChangeTest() {
		LocaleManager manager = getManager();

		LocalizerTestProperties text = text(manager).localization();

		assertEquals("simple property value", text.simpleLocalized().toString());

		manager.setLocale(Locale.FRENCH);

		assertEquals("French simple property value", text.simpleLocalized().toString());
	}

	@Test
	public void localeChangeEventTest() {
		LocaleManager manager = getManager();

		LocalizerTestProperties text = text(manager).localization();

		IdentityProperty<String> result = new IdentityProperty<>();
		text.addObserver(t -> {
			result.set(t.simpleLocalized().toString());
		});

		manager.setLocale(Locale.FRENCH);

		Assert.assertNotNull(result);
		assertEquals("French simple property value", result.get());
	}

	@Test
	public void localeChangeStringEventTest() {
		LocaleManager manager = getManager();

		Localized<String> string = text(manager).localization().simpleLocalized();

		IdentityProperty<String> result = new IdentityProperty<>();
		string.addObserver(t -> {
			result.set(t);
		});

		manager.setLocale(Locale.FRENCH);

		Assert.assertNotNull(result);
		assertEquals("French simple property value", string.get());
		assertEquals("French simple property value", result.get());
	}

	@Test
	public void localeChangeStringNoEventTest() {
		LocaleManager manager = getManager();

		Localized<String> string = text(manager).localization().anotherSimpleLocalized();

		IdentityProperty<String> result = new IdentityProperty<>();
		string.addObserver(t -> {
			result.set(t);
		});

		manager.setLocale(Locale.FRENCH);

		Assert.assertNull(result.get());
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
	public void keyAppendTextTest() {
		TestProperties text = text(getManager());

		assertEquals("value of append", text.keyAppend("append").toString());
	}

	@Test
	public void multipleKeyAppendTextTest() {
		TestProperties text = text(getManager());

		assertEquals("value of multiple append", text.multipleKeyAppend("append.one", "append.two").toString());
	}

	@Test
	public void missingTextTest() {
		TestProperties text = text(getManager());

		assertEquals("?localizer.test/missing.method?", text.missingMethod().toString());
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

	@Test
	public void nestedTextOverrideTest() {
		TestProperties text = text(getManager());

		assertEquals("deeply nested text overriding value", text.nesting().deeply().deeplyNestedOverrideText().toString());
	}
}
