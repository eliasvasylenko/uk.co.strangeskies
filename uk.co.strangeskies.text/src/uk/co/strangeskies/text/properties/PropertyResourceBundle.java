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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A resource bundle with a configurable locale
 * 
 * @author Elias N Vasylenko
 */
public abstract class PropertyResourceBundle extends ResourceBundle {
	private final Locale locale;
	private final PropertyResourceStrategy strategy;
	private final PropertyResourceConfiguration<?> configuration;

	/**
	 * Create a resource bundle with the given initial locale.
	 * 
	 * @param locale
	 *          the initial locale
	 * @param strategy
	 *          the strategy responsible for initialising this resource
	 * @param configuration
	 *          the resource locations
	 */
	public PropertyResourceBundle(Locale locale, PropertyResourceStrategy strategy,
			PropertyResourceConfiguration<?> configuration) {
		this.locale = locale;
		this.strategy = strategy;
		this.configuration = configuration;
	}

	@Override
	protected abstract String handleGetObject(String key);

	/**
	 * @param locale
	 *          the new locale
	 * @return the previous locale
	 */
	public PropertyResourceBundle withLocale(Locale locale) {
		return getStrategy().findLocalizedResourceBundle(locale, getConfiguration());
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @return the strategy employed to locate the resources backing this bundle
	 */
	public PropertyResourceStrategy getStrategy() {
		return strategy;
	}

	/**
	 * @return the property resource locations
	 */
	public PropertyResourceConfiguration<?> getConfiguration() {
		return configuration;
	}

	/**
	 * Create a {@link PropertyResourceBundle localising resource bundle} over the
	 * given class loader and base names. The created resource bundle will behave
	 * as a delegate to a series of {@link ResourceBundle resource bundles} ,
	 * which in turn will behave according to
	 * {@link ResourceBundle#getBundle(String, Locale, ClassLoader)} with the
	 * given class loader, using the given locations as base names, and using the
	 * current locale of the {@link PropertyResourceBundle}. This locale may
	 * change, and the delegate resource bundles will be updated accordingly.
	 * 
	 * @param locale
	 *          the locale for the resource bundle
	 * @param resource
	 *          the base names of properties files to load, and the class loaders
	 *          they exist in
	 * @return a resource bundle over all resources at each given location
	 */
	public static PropertyResourceBundle getBundle(Locale locale, PropertyResourceConfiguration<?> resource) {
		return DefaultPropertyResourceStrategy.getInstance().findLocalizedResourceBundle(locale, resource);
	}
}
