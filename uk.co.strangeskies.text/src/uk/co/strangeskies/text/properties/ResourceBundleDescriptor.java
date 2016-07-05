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

public class ResourceBundleDescriptor {
	private final ClassLoader classLoader;
	private final String location;

	public ResourceBundleDescriptor(ClassLoader classLoader, String location) {
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
		if (!(obj instanceof ResourceBundleDescriptor))
			return false;

		ResourceBundleDescriptor that = (ResourceBundleDescriptor) obj;

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
