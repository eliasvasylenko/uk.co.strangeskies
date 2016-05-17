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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.List;
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
	 * As returned by {@link #getDefaultLocalizer()}.
	 */
	Localizer DEFAULT_LOCALIZER = getLocalizer(LocaleManager.getDefaultManager());

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
	 * {@link LocalizedResourceBundle#getBundle(Locale, List)} invoked with the
	 * given class loader and locations, and the current locale.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @param resources
	 *          the class loaders and base names to fetch resource bundles from
	 * @return an implementation of the accessor interface
	 */
	default <T extends LocalizedText<T>> T getLocalization(Class<T> accessor,
			List<? extends LocalizationResource> resources) {
		return getLocalization(accessor, LocalizedResourceBundle.getBundle(getLocale(), resources));
	}

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}. Resources are
	 * loading according to the {@link LocalizedResourceBundle} returned from
	 * {@link LocalizedResourceBundle#getBundle(Locale, List)} invoked with the
	 * given class loader and locations, and the current locale.
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
		return getLocalization(accessor,
				stream(locations).map(location -> new LocalizationResource(classLoader, location)).collect(toList()));
	}

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link LocalizedText}. Resources are
	 * loading according to the {@link LocalizedResourceBundle} returned from
	 * {@link LocalizedResourceBundle#getBundle(Locale, List)} invoked with the
	 * class loader of the given class, and the given locations.
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
	 * {@link LocalizedResourceBundle#getBundle(Locale, List)} invoked with the
	 * class loader of the given class, and the location derived by taking the
	 * given class name, and removing {@code Text} from the end if it is present.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link LocalizedText} we wish to implement
	 * @return an implementation of the accessor interface
	 */
	default <T extends LocalizedText<T>> T getLocalization(Class<T> accessor) {
		return getLocalization(accessor, removeTextPostfix(accessor.getName()));
	}

	/**
	 * Get a simple {@link Localizer} implementation over the
	 * {@link LocaleManager#getDefaultManager() system locale}.
	 * 
	 * @return a {@link Localizer} for the system locale
	 */
	static Localizer getDefaultLocalizer() {
		return DEFAULT_LOCALIZER;
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

	/**
	 * @param string
	 *          the string to modify
	 * @return the given string, with "Text" removed from the end if present and
	 *         if the result would not be empty
	 */
	static String removeTextPostfix(String string) {
		String TEXT_POSTFIX = "Text";

		if (string.endsWith(TEXT_POSTFIX) && !string.equals(TEXT_POSTFIX)) {
			string = string.substring(0, string.length() - TEXT_POSTFIX.length());
		}
		return string;
	}

	/**
	 * The default translation scheme to generate resource keys from methods.
	 * <p>
	 * The string {@code "Text"} is removed from the end of the class name if
	 * present, then the resulting class name and the method name are transformed
	 * according to the behaviour of {@link #getKeyText(String)} and joined in
	 * order about the {@code "."} character.
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
	 * @param arguments
	 *          the invocation arguments, which may contribute to the key as per
	 *          {@link AppendToLocalizationKey}
	 * @return the default resource key for the given method
	 */
	static String getKey(Method method, Object[] arguments) {
		StringBuilder builder = new StringBuilder();

		/*
		 * from class name
		 */
		String className = removeTextPostfix(method.getDeclaringClass().getSimpleName());
		builder.append(getKeyText(className)).append('.');

		/*
		 * from method name / annotation
		 */
		LocalizationKey keyAnnotation = method.getAnnotation(LocalizationKey.class);
		if (keyAnnotation != null) {
			builder.append(keyAnnotation.value());
		} else {
			builder.append(getKeyText(method.getName()));
		}

		/*
		 * from annotated arguments
		 */
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				AppendToLocalizationKey appendKeyAnnotation = method.getParameters()[i]
						.getAnnotation(AppendToLocalizationKey.class);
				if (appendKeyAnnotation != null) {
					builder.append('.').append(getKeyText(arguments[i].toString()));
				}
			}
		}

		return builder.toString();
	}

	/**
	 * The given string is split into words according to camel-case rules and put
	 * into lower-case, then each word is joined in order about the {@code "."}
	 * character. Non-alphanumeric characters are removed, and where they occur
	 * within words are replaced with the {@code "."} character.
	 * 
	 * @param string
	 *          the string to convert to key format
	 * @return the given string in the format of a key
	 */
	static String getKeyText(String string) {
		StringBuilder builder = new StringBuilder();

		int copiedToIndex = 0;
		boolean isPreviousStartOfWord = false;

		for (int i = 0; i < string.length(); i++) {
			char character = string.charAt(i);
			int copyToIndex = copiedToIndex;

			boolean isAlphanumeric = Character.isAlphabetic(character) || Character.isDigit(character);
			boolean isStartOfWord = isAlphanumeric && Character.isUpperCase(character) || Character.isDigit(character);

			if (!isAlphanumeric || (isStartOfWord && !isPreviousStartOfWord)) {
				copyToIndex = i;
			} else if (!isStartOfWord && isPreviousStartOfWord) {
				copyToIndex = i - 1;
			}

			isPreviousStartOfWord = isStartOfWord;

			if (copyToIndex > copiedToIndex) {
				if (builder.length() > 0) {
					builder.append('.');
				}
				builder.append(string.substring(copiedToIndex, copyToIndex));
				copiedToIndex = copyToIndex;
			}

			if (!isAlphanumeric) {
				copiedToIndex++;
			}
		}

		if (builder.length() > 0) {
			builder.append('.');
		}
		builder.append(string.substring(copiedToIndex));

		return builder.toString().toLowerCase();
	}
}
