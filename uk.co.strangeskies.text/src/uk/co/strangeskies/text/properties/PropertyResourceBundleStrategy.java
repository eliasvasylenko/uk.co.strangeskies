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

public class PropertyResourceBundleStrategy implements PropertyResourceStrategy<PropertyResourceBundleStrategy> {
	private static final PropertyResourceBundleStrategy INSTANCE = new PropertyResourceBundleStrategy();

	private PropertyResourceBundleStrategy() {}

	@Override
	public Class<PropertyResourceBundleStrategy> strategyClass() {
		return PropertyResourceBundleStrategy.class;
	}

	@Override
	public <T extends Properties<T>> PropertyResource getPropertyResourceBundle(Class<T> accessor, String resource) {
		return new PropertyResourceImpl(this, accessor, resource);
	}

	/**
	 * @return an instance of the strategy
	 */
	public static final PropertyResourceBundleStrategy getInstance() {
		return INSTANCE;
	}
}
