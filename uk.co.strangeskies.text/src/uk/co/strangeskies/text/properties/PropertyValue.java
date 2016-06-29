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
import java.util.Optional;

/**
 * A {@link PropertyValue} instance represents the value returned from a method
 * of an {@link Properties} accessor class. The value may be dependent on the
 * arguments passed to this method, so an instantiation can be produced via
 * {@link #instantiate(List)}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the property value
 */
public interface PropertyValue<T> {
	/**
	 * @param arguments
	 *          the arguments passed to the property accessor method
	 * @return the instantiation of the value for the given arguments
	 */
	Optional<T> instantiate(List<?> arguments);
}
