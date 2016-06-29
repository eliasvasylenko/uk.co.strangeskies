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

import java.util.List;
import java.util.Locale;

/**
 * A default implementation of {@link PropertyResourceStrategy} which creates
 * {@link PropertyResourceBundle} instances over given resource location strings
 * and locales.
 * 
 * @author Elias N Vasylenko
 */
public class DefaultPropertyResourceStrategy implements PropertyResourceStrategy {
	private static final DefaultPropertyResourceStrategy INSTANCE = new DefaultPropertyResourceStrategy();

	private DefaultPropertyResourceStrategy() {}

	@Override
	public PropertyResourceBundle findLocalizedResourceBundle(Locale initialLocale,
			List<? extends PropertyAccessorConfiguration<?>> resourceDescription) {
		return new PropertyResourceBundleImpl(initialLocale, this, resourceDescription);
	}

	/**
	 * @return an instance of the strategy
	 */
	public static final DefaultPropertyResourceStrategy getInstance() {
		return INSTANCE;
	}
}
