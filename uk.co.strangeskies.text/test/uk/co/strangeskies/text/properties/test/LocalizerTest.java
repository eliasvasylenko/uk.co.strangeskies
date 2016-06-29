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
public class LocalizerTest {
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

	public LocalizerTestProperties text(LocaleManager manager) {
		return PropertyLoader.getPropertyLoader(manager).getProperties(LocalizerTestProperties.class);
	}

	@Test
	public void localizerTest() {
		getManager();
	}

	@Test
	public void localizationTest() {
		text(getManager());
	}

	@Test
	public void simpleTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("simple property value", text.simple().toString());
	}

	@Test
	public void languageTest() {
		LocalizerTestProperties text = text(getManager(Locale.FRENCH));

		assertEquals("French simple property value", text.simple().toString());
	}

	@Test
	public void languageDefaultTest() {
		LocalizerTestProperties text = text(getManager(Locale.FRENCH));

		assertEquals("another simple property value", text.anotherSimple().toString());
	}

	@Test
	public void languageChangeTest() {
		LocaleManager manager = getManager();

		LocalizerTestProperties text = text(manager);

		assertEquals("simple property value", text.simple().toString());

		manager.setLocale(Locale.FRENCH);

		assertEquals("French simple property value", text.simple().toString());
	}

	@Test
	public void localeChangeEventTest() {
		LocaleManager manager = getManager();

		LocalizerTestProperties text = text(manager);

		IdentityProperty<String> result = new IdentityProperty<>();
		text.addObserver(t -> {
			result.set(t.simple().toString());
		});

		manager.setLocale(Locale.FRENCH);

		Assert.assertNotNull(result);
		assertEquals("French simple property value", result.get());
	}

	@Test
	public void localeChangeStringEventTest() {
		LocaleManager manager = getManager();

		Localized<String> string = text(manager).simple();

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

		Localized<String> string = text(manager).anotherSimple();

		IdentityProperty<String> result = new IdentityProperty<>();
		string.addObserver(t -> {
			result.set(t);
		});

		manager.setLocale(Locale.FRENCH);

		Assert.assertNull(result.get());
	}

	@Test
	public void substitutionTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("value of substitution", text.substitution("substitution").toString());
	}

	@Test
	public void multipleSubstitutionTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("values of substitution one and substitution two",
				text.multipleSubstitution("substitution one", "substitution two").toString());
	}

	@Test
	public void keyAppendTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("value of append", text.keyAppend("append").toString());
	}

	@Test
	public void multipleKeyAppendTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("value of multiple append", text.multipleKeyAppend("append.one", "append.two").toString());
	}

	@Test
	public void missingTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("?localizer.test/missing.method?", text.missingMethod().toString());
	}

	@Test
	public void defaultTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("value of default", text.defaultMethod().toString());
	}

	@Test
	public void copyTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("simple property value", text.copy().simple().toString());
	}

	@Test
	public void nestedTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("nested text value", text.nested().nestedText().toString());
	}

	@Test
	public void deeplyNestedTextTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("deeply nested text value", text.nested().deeply().deeplyNestedText().toString());
	}

	@Test
	public void nestedTextOverrideTest() {
		LocalizerTestProperties text = text(getManager());

		assertEquals("deeply nested text overriding value", text.nested().deeply().deeplyNestedOverrideText().toString());
	}
}
