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

/**
 * A {@link LocalizedText} interface to provide localized texts for use by the
 * {@link Localizer} class itself, such as for reporting errors for improperly
 * structured localisation classes, etc.
 * 
 * @author Elias N Vasylenko
 */
public interface LocalizerText extends LocalizedText<LocalizerText> {
	/**
	 * @param accessor
	 *          the accessor to localise
	 * @return the accessor class must be an interface
	 */
	LocalizedString mustBeInterface(Class<?> accessor);

	/**
	 * @param accessor
	 *          the accessor to localise
	 * @param method
	 *          the method with the illegal return type
	 * @param stringClass
	 *          the expected return type
	 * @return the method must return the expected type
	 */
	LocalizedString illegalReturnType(Class<?> accessor, Method method, Class<LocalizedString> stringClass);

	/**
	 * @param accessor
	 *          the accessor to localise
	 * @param method
	 *          the method with the illegal return type
	 * @return the method must return the expected type
	 */
	default LocalizedString illegalReturnType(Class<?> accessor, Method method) {
		return illegalReturnType(accessor, method, LocalizedString.class);
	}

	/**
	 * @param key
	 *          the method to find a translation for
	 * @return substitution when no translation is found for the given method
	 */
	LocalizedString translationNotFoundFor(String key);

	/**
	 * @param method
	 *          the method to find a translation for
	 * @return substitution when no translation is found for the given method
	 */
	default LocalizedString translationNotFoundFor(Method method) {
		return translationNotFoundFor(LocalizerImpl.getKey(method));
	}

	/**
	 * @param key
	 *          the method to find a translation for
	 * @return no translation for the given method
	 */
	LocalizedString translationNotFoundMessage(String key);

	/**
	 * @param method
	 *          the method to find a translation for
	 * @return no translation for the given method
	 */
	default LocalizedString translationNotFoundMessage(Method method) {
		return translationNotFoundMessage(LocalizerImpl.getKey(method));
	}

	/**
	 * @param manager
	 *          manager
	 * @param locale
	 *          the new locale
	 * @return locale has been changed for manager
	 */
	LocalizedString localeChanged(LocaleManager manager, Locale locale);
}
