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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static uk.co.strangeskies.utilities.text.LocaleManager.getManager;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.text.AppendToLocalizationKey;
import uk.co.strangeskies.utilities.text.LocaleManager;
import uk.co.strangeskies.utilities.text.LocalizationKey;
import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.Localizer;

@SuppressWarnings("javadoc")
public class LocalizerTest {
	private static final String KEY_TRANSLATION_METHODS = "key.translation.methods";

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

	private enum Append {
		APPEND_ME, APPEND_THIS_TOO
	}

	private interface KeyTranslationMethods {
		void singleword();

		void twoWords();

		void CapitalStart();

		void MULTIPLECapitalStart();

		void multipleCAPITALSMiddle();

		void capitalAtEND();

		void capitalNearEndIs();

		void singleCapitalEndI();

		void with_underscore();

		void with___multipleUnderscores();

		void capitalBEFORE_underscore();

		void capitalA_underscore();

		void withNumber1();

		@LocalizationKey("this.is.the.key")
		void annotated();

		void withAppend(@AppendToLocalizationKey Append a);

		@LocalizationKey("this.is.the.key")
		void annotatedWithAppend(@AppendToLocalizationKey Append a);

		void appendMultiple(@AppendToLocalizationKey Append a, @AppendToLocalizationKey Append b);
	}

	private static class KeyTranslation {
		final String string;
		final String key;
		final Object[] append;

		public KeyTranslation(String string, String key, Object... append) {
			this.string = string;
			this.key = key;
			this.append = append;
		}
	}

	private static final Collection<KeyTranslation> KEY_TRANSLATIONS = asList(
			new KeyTranslation("singleword", "singleword"),

			new KeyTranslation("twoWords", "two.words"),

			new KeyTranslation("CapitalStart", "capital.start"),

			new KeyTranslation("MULTIPLECapitalStart", "multiple.capital.start"),

			new KeyTranslation("multipleCAPITALSMiddle", "multiple.capitals.middle"),

			new KeyTranslation("capitalAtEND", "capital.at.end"),

			new KeyTranslation("capitalNearEndIs", "capital.near.end.is"),

			new KeyTranslation("singleCapitalEndI", "single.capital.end.i"),

			new KeyTranslation("with_underscore", "with.underscore"),

			new KeyTranslation("with___multipleUnderscores", "with.multiple.underscores"),

			new KeyTranslation("capitalBEFORE_underscore", "capital.before.underscore"),

			new KeyTranslation("capitalA_underscore", "capital.a.underscore"),

			new KeyTranslation("annotated", "this.is.the.key"),

			new KeyTranslation("withAppend", "with.append.append.me", Append.APPEND_ME),

			new KeyTranslation("annotatedWithAppend", "this.is.the.key.append.me", Append.APPEND_ME),

			new KeyTranslation("appendMultiple", "append.multiple.append.me.append.this.too", Append.APPEND_ME,
					Append.APPEND_THIS_TOO));

	public LocalizerTestText text(LocaleManager manager) {
		return Localizer.getLocalizer(manager).getLocalization(LocalizerTestText.class);
	}

	@Test
	public void keyTranslationTest() throws NoSuchMethodException, SecurityException {
		for (KeyTranslation translation : KEY_TRANSLATIONS) {
			Class<?>[] parameterTypes = new Class<?>[translation.append.length];
			for (int i = 0; i < translation.append.length; i++) {
				parameterTypes[i] = translation.append[i].getClass();
			}

			assertEquals(KEY_TRANSLATION_METHODS + "." + translation.key, Localizer
					.getKey(KeyTranslationMethods.class.getMethod(translation.string, parameterTypes), translation.append));
		}
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
		LocalizerTestText text = text(getManager());

		assertEquals("simple property value", text.simple().toString());
	}

	@Test
	public void languageTest() {
		LocalizerTestText text = text(getManager(Locale.FRENCH));

		assertEquals("French simple property value", text.simple().toString());
	}

	@Test
	public void languageDefaultTest() {
		LocalizerTestText text = text(getManager(Locale.FRENCH));

		assertEquals("another simple property value", text.anotherSimple().toString());
	}

	@Test
	public void languageChangeTest() {
		LocaleManager manager = getManager();

		LocalizerTestText text = text(manager);

		assertEquals("simple property value", text.simple().toString());

		manager.setLocale(Locale.FRENCH);

		assertEquals("French simple property value", text.simple().toString());
	}

	@Test
	public void localeChangeEventTest() {
		LocaleManager manager = getManager();

		LocalizerTestText text = text(manager);

		IdentityProperty<String> result = new IdentityProperty<>();
		Consumer<LocalizerTestText> observer = t -> {
			result.set(t.simple().toString());
		};
		text.addObserver(observer);

		manager.setLocale(Locale.FRENCH);

		Assert.assertNotNull(result);
		assertEquals("French simple property value", result.get());
	}

	@Test
	public void localeChangeStringEventTest() {
		LocaleManager manager = getManager();

		LocalizedString string = text(manager).simple();

		IdentityProperty<String> result = new IdentityProperty<>();
		Consumer<String> observer = t -> {
			result.set(t);
		};
		string.addObserver(observer);

		manager.setLocale(Locale.FRENCH);

		Assert.assertNotNull(result);
		assertEquals("French simple property value", result.get());
	}

	@Test
	public void localeChangeStringNoEventTest() {
		LocaleManager manager = getManager();

		LocalizedString string = text(manager).anotherSimple();

		IdentityProperty<String> result = new IdentityProperty<>();
		Consumer<String> observer = t -> {
			result.set(t);
		};
		string.addObserver(observer);

		manager.setLocale(Locale.FRENCH);

		Assert.assertNull(result.get());
	}

	@Test
	public void substitutionTextTest() {
		LocalizerTestText text = text(getManager());

		assertEquals("value of substitution", text.substitution("substitution").toString());
	}

	@Test
	public void missingTextTest() {
		LocalizerTestText text = text(getManager());

		assertEquals("?localizer.test.missing.method?", text.missingMethod().toString());
	}

	@Test
	public void defaultTextTest() {
		LocalizerTestText text = text(getManager());

		assertEquals("value of default", text.defaultMethod().toString());
	}

	@Test
	public void copyTextTest() {
		LocalizerTestText text = text(getManager());

		assertEquals("simple property value", text.copy().simple().toString());
	}

	@Test
	public void nestedTextTest() {
		LocalizerTestText text = text(getManager());

		assertEquals("nested text value", text.nested().nestedText().toString());
	}

	@Test
	public void deeplyNestedTextTest() {
		LocalizerTestText text = text(getManager());

		assertEquals("deeply nested text value", text.nested().deeply().deeplyNestedText().toString());
	}

	@Test
	public void nestedTextOverrideTest() {
		LocalizerTestText text = text(getManager());

		assertEquals("deeply nested text overriding value", text.nested().deeply().deeplyNestedOverrideText().toString());
	}
}
