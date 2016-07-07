/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi;

/**
 * A very simple wrapper around an OSGi service which exposes the service
 * ranking. The ranking is not guaranteed to remain consistent.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the service
 */
public interface RankedService<T> {
	/**
	 * @return the wrapped service object
	 */
	T getServiceObject();

	/**
	 * @return the current ranking of the object
	 */
	int getRanking();
}
