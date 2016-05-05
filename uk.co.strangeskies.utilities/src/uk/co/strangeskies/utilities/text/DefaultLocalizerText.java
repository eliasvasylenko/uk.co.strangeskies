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
package uk.co.strangeskies.utilities.text;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.Consumer;

final class DefaultLocalizerText implements LocalizerText {
	private static final String TRANSLATION_NOT_FOUND = "?";
	private static final String TRANSLATION_NOT_FOUND_MESSAGE = "Translation not found for method";
	private static final String MUST_BE_INTERFACE = "Localization accessor must be interface";
	private static final String ILLEGAL_RETURN_TYPE = "Return type must be " + LocalizedString.class.getName();
	private static final String LOCALE_CHANGED = "Locale changed";

	@Override
	public Locale getLocale() {
		return Locale.ENGLISH;
	}

	@Override
	public LocalizedString translationNotFoundFor(String key) {
		return englishString(TRANSLATION_NOT_FOUND + key + TRANSLATION_NOT_FOUND);
	}

	@Override
	public LocalizedString translationNotFoundMessage(String key) {
		return englishString(TRANSLATION_NOT_FOUND_MESSAGE + ": " + key);
	}

	@Override
	public LocalizedString mustBeInterface(Class<?> accessor) {
		return englishString(MUST_BE_INTERFACE + ": " + accessor);
	}

	@Override
	public LocalizedString illegalReturnType(Class<?> accessor, Method method, Class<LocalizedString> stringClass) {
		return englishString(ILLEGAL_RETURN_TYPE + ": " + method);
	}

	@Override
	public LocalizedString localeChanged(LocaleProvider manager, Locale locale) {
		return englishString(LOCALE_CHANGED + ": " + locale);
	}

	private LocalizedString englishString(String text) {
		return new LocalizedString() {
			@Override
			public String toString() {
				return text;
			}

			@Override
			public boolean removeObserver(Consumer<? super String> observer) {
				return true;
			}

			@Override
			public boolean addObserver(Consumer<? super String> observer) {
				return true;
			}

			@Override
			public Locale locale() {
				return Locale.ENGLISH;
			}

			@Override
			public String toString(Locale locale) {
				return toString();
			}
		};
	}

	@Override
	public boolean addObserver(Consumer<? super LocalizerText> observer) {
		return true;
	}

	@Override
	public boolean removeObserver(Consumer<? super LocalizerText> observer) {
		return true;
	}
}
