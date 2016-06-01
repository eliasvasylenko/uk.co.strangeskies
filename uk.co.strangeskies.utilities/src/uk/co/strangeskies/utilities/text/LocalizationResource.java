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

/**
 * A class loader and base name location referring to a unique localisation
 * resource.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
public class LocalizationResource {
	private final ClassLoader classLoader;
	private final String location;

	public LocalizationResource(ClassLoader classLoader, String location) {
		this.classLoader = classLoader;
		this.location = location;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String getLocation() {
		return location;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof LocalizationResource))
			return false;

		LocalizationResource that = (LocalizationResource) obj;

		return this.classLoader.equals(that.classLoader) && this.location.equals(that.location);
	}

	@Override
	public int hashCode() {
		return classLoader.hashCode() ^ location.hashCode();
	}

	@Override
	public String toString() {
		return location;
	}
}
