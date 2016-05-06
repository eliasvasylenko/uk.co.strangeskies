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
import java.util.ResourceBundle;

import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.ObservableValue;

/**
 * This interface represents a simple but powerful system for
 * internationalisation. Instances of this class provide automatic
 * implementations of sub-interfaces of {@link LocalizedText} according to a
 * {@link Locale} setting, which delegate method invocations to be fetched from
 * {@link ResourceBundle resource bundles}.
 * 
 * @author Elias N Vasylenko
 */
public interface Localizer {
	/**
	 * @return the current locale of all localised texts implemented by this
	 *         {@link Localizer}
	 */
	Locale getLocale();

	/**
	 * @return an observable over changes to the locale
	 */
	ObservableValue<Locale> locale();

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}, and with
	 * resource values taken from the given resource bundle.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @param bundle
	 *          the resource bundle with which to load resources
	 * @return an implementation of the accessor interface
	 */
	<T extends LocalizedText<T>> T getLocalization(Class<T> accessor, LocalizedResourceBundle bundle);

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}. Resources are
	 * loading according to the {@link LocalizedResourceBundle} returned from
	 * {@link LocalizedResourceBundle#getBundle(ClassLoader, Locale, String[])}
	 * invoked with the given class loader and locations, and the current locale.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @param classLoader
	 *          the class loader to fetch resource bundles from
	 * @param locations
	 *          the base names of all backing resource bundles
	 * @return an implementation of the accessor interface
	 */
	default <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, ClassLoader classLoader,
			String... locations) {
		return getLocalization(accessor, LocalizedResourceBundle.getBundle(classLoader, getLocale(), locations));
	}

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}. Resources are
	 * loading according to the {@link LocalizedResourceBundle} returned from
	 * {@link LocalizedResourceBundle#getBundle(ClassLoader, Locale, String[])}
	 * invoked with the class loader of the given class, and the given locations.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @param locations
	 *          the base names of all backing resource bundles
	 * @return an implementation of the accessor interface
	 */
	default <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, String... locations) {
		return getLocalization(accessor, accessor.getClassLoader(), locations);
	}

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}. Resources are
	 * loading according to the {@link LocalizedResourceBundle} returned from
	 * {@link LocalizedResourceBundle#getBundle(ClassLoader, Locale, String[])}
	 * invoked with the class loader of the given class, and the location derived
	 * by taking the given class name, and removing {@code Text} from the end if
	 * it is present.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @return an implementation of the accessor interface
	 */
	default <T extends LocalizedText<T>> T getLocalization(Class<T> accessor) {
		return getLocalization(accessor, LocalizerImpl.removeTextPostfix(accessor.getName()));
	}

	/**
	 * The default translation scheme to generate resource keys from methods.
	 * <p>
	 * The class name and method name are split into words according to camel-case
	 * rules and put into lower-case, then the word {@code "text"} is removed from
	 * the end of the class name if present, then each word from the class and
	 * each word from the method are joined in order about the {@code "."}
	 * character.
	 * <p>
	 * For example the method
	 * {@link LocalizerText#illegalReturnType(Class, java.lang.reflect.Method)}
	 * would generate the key {@code "localizer.illegal.return.type"}. The key
	 * generation rules can be overridden for a given method by annotating with
	 * {@link LocalizationKey}.
	 * 
	 * @param method
	 *          the method for which we wish to determine an associated resource
	 *          key
	 * @return the default resource key for the given method
	 */
	static String getKey(Method method, Object... arguments) {
		return LocalizerImpl.getKey(method, arguments);
	}

	/**
	 * Get a simple {@link Localizer} implementation over the given locale
	 * provider.
	 * 
	 * @param provider
	 *          a provider to establish a locale setting
	 * @return a {@link Localizer} for the given locale
	 */
	static Localizer getLocalizer(LocaleProvider provider) {
		return getLocalizer(provider, null);
	}

	/**
	 * Get a simple {@link Localizer} implementation over the given locale
	 * provider.
	 * 
	 * @param provider
	 *          a provider to establish a locale setting
	 * @param log
	 *          a log for localisation information
	 * @return a {@link Localizer} for the given locale
	 */
	static Localizer getLocalizer(LocaleProvider provider, Log log) {
		return new LocalizerImpl(provider, log);
	}
}
