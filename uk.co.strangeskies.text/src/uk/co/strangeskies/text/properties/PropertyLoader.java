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

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.ObservableValue;

/**
 * This interface represents a simple but powerful system for
 * internationalisation. Instances of this class provide automatic
 * implementations of sub-interfaces of {@link Properties} according to a
 * {@link Locale} setting, which delegate method invocations to be fetched from
 * {@link ResourceBundle resource bundles}.
 * 
 * @author Elias N Vasylenko
 */
public interface PropertyLoader {
	String TEXT_POSTFIX = "Properties";

	/**
	 * As returned by {@link #getDefaultLocalizer()}.
	 */
	PropertyLoader DEFAULT_PROPERTY_LOADER = getPropertyLoader(LocaleManager.getDefaultManager());

	/**
	 * @return the current locale of all localised texts implemented by this
	 *         {@link PropertyLoader}
	 */
	Locale getLocale();

	/**
	 * @return an observable over changes to the locale
	 */
	ObservableValue<Locale> locale();

	/**
	 * @param propertyProvider
	 *          a provider for a type of property
	 * @return true if the provider was registered, false otherwise
	 */
	boolean registerProvider(PropertyValueProviderFactory propertyProvider);

	/**
	 * 
	 * @param propertyProvider
	 *          a provider for a type of property
	 * @return true if the provider was unregistered, false otherwise
	 */
	boolean unregisterProvider(PropertyValueProviderFactory propertyProvider);

	/**
	 * @return all available property providers
	 */
	List<PropertyValueProviderFactory> getProviders();

	/**
	 * @return all available property providers
	 */
	Optional<PropertyValueProvider<?>> getProvider(AnnotatedType type);

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link Properties}.
	 * 
	 * @param accessor
	 *          the sub-interface of {@link Properties} we wish to implement
	 * @param defaultConfiguration
	 *          the default property configuration to apply
	 * @return an implementation of the accessor interface
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 */
	<T extends Properties<T>> T getProperties(Class<T> accessor, PropertyConfiguration defaultConfiguration);

	/**
	 * Generate an implementing instance of the given accessor interface class,
	 * according to the rules described by {@link Properties}.
	 * 
	 * @param <T>
	 *          the type of the localisation text accessor interface
	 * @param accessor
	 *          the sub-interface of {@link Properties} we wish to implement
	 * @return an implementation of the accessor interface
	 */
	<T extends Properties<T>> T getProperties(Class<T> accessor);

	/**
	 * Get a simple {@link PropertyLoader} implementation over the
	 * {@link LocaleManager#getDefaultManager() system locale}.
	 * 
	 * @return a {@link PropertyLoader} for the system locale
	 */
	static PropertyLoader getDefaultLocalizer() {
		return DEFAULT_PROPERTY_LOADER;
	}

	/**
	 * Get a simple {@link PropertyLoader} implementation over the given locale
	 * provider.
	 * 
	 * @param provider
	 *          a provider to establish a locale setting
	 * @return a {@link PropertyLoader} for the given locale
	 */
	static PropertyLoader getPropertyLoader(LocaleProvider provider) {
		return getPropertyLoader(provider, null);
	}

	/**
	 * Get a simple {@link PropertyLoader} implementation over the given locale
	 * provider.
	 * 
	 * @param provider
	 *          a provider to establish a locale setting
	 * @param log
	 *          a log for localisation information
	 * @return a {@link PropertyLoader} for the given locale
	 */
	static PropertyLoader getPropertyLoader(LocaleProvider provider, Log log) {
		return new PropertyLoaderImpl(provider, log);
	}

	static String removePropertiesPostfix(String name) {
		if (name.endsWith(TEXT_POSTFIX) && !name.equals(TEXT_POSTFIX)) {
			name = name.substring(0, name.length() - TEXT_POSTFIX.length());
		}

		return name;
	}
}
