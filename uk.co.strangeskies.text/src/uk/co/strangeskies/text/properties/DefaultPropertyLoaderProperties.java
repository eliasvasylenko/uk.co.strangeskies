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
package uk.co.strangeskies.text.properties;

import static java.lang.String.format;

import java.lang.reflect.Type;
import java.util.Locale;

final class DefaultPropertyLoaderProperties extends StaticPropertyAccessor<PropertyLoaderProperties>
		implements PropertyLoaderProperties {
	private static final String TRANSLATION_NOT_FOUND = "?%s?";
	private static final String TRANSLATION_NOT_FOUND_MESSAGE = "Translation not found for key %s";
	private static final String MUST_BE_INTERFACE = "Localization accessor %s must be an interface";
	private static final String ILLEGAL_RETURN_TYPE = "Illegal return type %s for key %s";
	private static final String LOCALE_CHANGED = "Locale changed to %s";
	private static final String CANNOT_INSTANTIATE_STRATEGY = "Cannot instantiate strategy %s";

	public DefaultPropertyLoaderProperties() {
		super(Locale.ENGLISH);
	}

	@Override
	public String translationNotFoundSubstitution(String key) {
		return format(TRANSLATION_NOT_FOUND, key);
	}

	@Override
	public Localized<String> translationNotFoundMessage(String key) {
		return localize(TRANSLATION_NOT_FOUND_MESSAGE, key);
	}

	@Override
	public Localized<String> mustBeInterface(Class<?> accessor) {
		return localize(MUST_BE_INTERFACE, accessor);
	}

	@Override
	public Localized<String> illegalReturnType(Type type, String key) {
		return localize(ILLEGAL_RETURN_TYPE, type, key);
	}

	@Override
	public Localized<String> localeChanged(LocaleProvider manager, Locale locale) {
		return localize(LOCALE_CHANGED, locale);
	}

	@Override
	public Localized<String> cannotInstantiateStrategy(Class<? extends PropertyResourceStrategy> strategy) {
		return localize(CANNOT_INSTANTIATE_STRATEGY, strategy);
	}
}
