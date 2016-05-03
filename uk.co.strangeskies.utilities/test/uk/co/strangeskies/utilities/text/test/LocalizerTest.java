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
import static uk.co.strangeskies.utilities.text.Localizer.getKey;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.Localizer;

@SuppressWarnings("javadoc")
public class LocalizerTest {
	private static final String KEY_TRANSLATION_METHODS = "key.translation.methods";

	private interface KeyTranslationMethods {
		void singleword();

		void twoWords();

		void CapitalStart();

		void MULTIPLECapitalStart();

		void multipleCAPITALSMiddle();

		void capitalAtEND();

		void capitalNearEndIs();

		void singleCapitalEndI();
	}

	private static class KeyTranslation {
		public KeyTranslation(String string, String key) {
			this.string = string;
			this.key = key;
		}

		final String string;
		final String key;
	}

	private static final Collection<KeyTranslation> KEY_TRANSLATIONS = asList(
			new KeyTranslation("singleword", "singleword"),

			new KeyTranslation("twoWords", "two.words"),

			new KeyTranslation("CapitalStart", "capital.start"),

			new KeyTranslation("MULTIPLECapitalStart", "multiple.capital.start"),

			new KeyTranslation("multipleCAPITALSMiddle", "multiple.capitals.middle"),

			new KeyTranslation("capitalAtEND", "capital.at.end"),

			new KeyTranslation("capitalNearEndIs", "capital.near.end.is"),

			new KeyTranslation("singleCapitalEndI", "single.capital.end.i"));

	public LocalizerTestText text(Localizer localizer) {
		return localizer.getLocalization(LocalizerTestText.class);
	}

	@Test
	public void keyTranslationTest() throws NoSuchMethodException, SecurityException {
		for (KeyTranslation translation : KEY_TRANSLATIONS) {
			assertEquals(KEY_TRANSLATION_METHODS + "." + translation.key,
					getKey(KeyTranslationMethods.class.getMethod(translation.string)));
		}
	}

	@Test
	public void localizerTest() {
		new Localizer();
	}

	@Test
	public void localizationTest() {
		text(new Localizer());
	}

	@Test
	public void simpleTextTest() {
		LocalizerTestText text = text(new Localizer());

		assertEquals("simple property value", text.simple().toString());
	}

	@Test
	public void languageTest() {
		LocalizerTestText text = text(new Localizer(Locale.FRENCH));

		assertEquals("French simple property value", text.simple().toString());
	}

	@Test
	public void languageDefaultTest() {
		LocalizerTestText text = text(new Localizer(Locale.FRENCH));

		assertEquals("another simple property value", text.anotherSimple().toString());
	}

	@Test
	public void languageChangeTest() {
		Localizer localizer = new Localizer();

		LocalizerTestText text = text(localizer);

		assertEquals("simple property value", text.simple().toString());

		localizer.setLocale(Locale.FRENCH);

		assertEquals("French simple property value", text.simple().toString());
	}

	@Test
	public void localeChangeEventTest() {
		Localizer localizer = new Localizer();

		LocalizerTestText text = text(localizer);

		IdentityProperty<String> result = new IdentityProperty<>();
		Consumer<LocalizerTestText> observer = t -> {
			result.set(t.simple().toString());
		};
		text.addObserver(observer);

		localizer.setLocale(Locale.FRENCH);

		Assert.assertNotNull(result);
		assertEquals("French simple property value", result.get());
	}

	@Test
	public void localeChangeStringEventTest() {
		Localizer localizer = new Localizer();

		LocalizedString string = text(localizer).simple();

		IdentityProperty<String> result = new IdentityProperty<>();
		Consumer<String> observer = t -> {
			result.set(t);
		};
		string.addObserver(observer);

		localizer.setLocale(Locale.FRENCH);

		Assert.assertNotNull(result);
		assertEquals("French simple property value", result.get());
	}

	@Test
	public void localeChangeStringNoEventTest() {
		Localizer localizer = new Localizer();

		LocalizedString string = text(localizer).anotherSimple();

		IdentityProperty<String> result = new IdentityProperty<>();
		Consumer<String> observer = t -> {
			result.set(t);
		};
		string.addObserver(observer);

		localizer.setLocale(Locale.FRENCH);

		Assert.assertNull(result.get());
	}

	@Test
	public void substitutionTextTest() {
		LocalizerTestText text = text(new Localizer());

		assertEquals("value of substitution", text.substitution("substitution").toString());
	}

	@Test
	public void missingTextTest() {
		LocalizerTestText text = text(new Localizer());

		assertEquals("?localizer.test.missing.method?", text.missingMethod().toString());
	}

	@Test
	public void defaultTextTest() {
		LocalizerTestText text = text(new Localizer());

		assertEquals("value of default", text.defaultMethod().toString());
	}

	@Test
	public void copyTextTest() {
		LocalizerTestText text = text(new Localizer());

		assertEquals("simple property value", text.copy().simple().toString());
	}
}
