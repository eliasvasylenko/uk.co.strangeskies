/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.utilities;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class identifies a set of properties relating to a certain class, such
 * that they can be used to more easily implement hash code generation and
 * equality testing.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the objects whose properties we wish to identify.
 */
public class PropertySet<T> {
	private final Set<Function<? super T, Object>> properties;
	private final Class<T> objectClass;

	/**
	 * Create a PropertySet over the given class.
	 * 
	 * @param objectClass
	 *          The class of object this set of properties is applicable to.
	 */
	public PropertySet(Class<T> objectClass) {
		this.objectClass = objectClass;

		properties = new LinkedHashSet<>();
	}

	/**
	 * Add a property to the set.
	 * 
	 * @param property
	 *          A function describing a property as a transformation from an
	 *          object instance to that properties value for that instance.
	 * @return The property set the property has been added to.
	 */
	public PropertySet<T> add(Function<? super T, Object> property) {
		properties.add(property);
		return this;
	}

	/**
	 * Add a set of properties to the set.
	 * 
	 * @param properties
	 *          The set of properties to add.
	 * @return The property set the properties have been added to.
	 */
	public PropertySet<T> add(PropertySet<? super T> properties) {
		for (Function<? super T, Object> property : properties.properties)
			this.properties.add(property);
		return this;
	}

	/**
	 * Retrieve the class of objects this property set is applicable to.
	 * 
	 * @return The object class.
	 */
	public Class<T> objectClass() {
		return objectClass;
	}

	/**
	 * Retrieve the list of property values for the given object of applicable
	 * class.
	 * 
	 * @param object
	 *          The instance for which to retrieve values.
	 * @return A list of values in the order they were added to the property set.
	 */
	public List<Object> values(T object) {
		return properties.stream().map(p -> p.apply(object))
				.collect(Collectors.toList());
	}

	/**
	 * Compare the given object of applicable class to another object, to test for
	 * equality.
	 * 
	 * @param first
	 *          The first object, of applicable class.
	 * @param second
	 *          The second object, of any class.
	 * @return True if the objects are equal according to the equality of each
	 *         property in this set, false otherwise.
	 */
	public boolean testEquality(T first, Object second) {
		if (first == second)
			return true;

		if (!objectClass.isInstance(second))
			return false;

		@SuppressWarnings("unchecked")
		T secondObject = (T) second;
		return properties.stream().allMatch(
				p -> Objects.equals(p.apply(first), p.apply(secondObject)));
	}

	/**
	 * Calculate the hash code of a given object of the applicable class.
	 * 
	 * @param object
	 *          The object for which to calculate the hash code.
	 * @return The hash code, derived from the hash codes of the values of each
	 *         property in this set.
	 */
	public int generateHashCode(T object) {
		return properties.stream().map(p -> Objects.hashCode(p.apply(object)))
				.reduce(0, (a, b) -> a ^ b);
	}

	/**
	 * String output for an object of the applicable class, based on the toString
	 * implementations of each value in the set.
	 * 
	 * @param object
	 *          The object to give a String representation of.
	 * @return A string representation based on the values of each property in
	 *         this set.
	 */
	public String toString(T object) {
		String lineSeparator = System.getProperty("line.separator");

		StringBuilder stringBuilder = new StringBuilder().append("{").append(
				lineSeparator);
		for (Function<? super T, Object> property : properties)
			stringBuilder.append("  : ").append(property.apply(object))
					.append(lineSeparator);
		stringBuilder.append("}");

		return stringBuilder.toString();
	}
}
