/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2.
 *
 * uk.co.strangeskies.p2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2;

import uk.co.strangeskies.utilities.Log;

/**
 * A factory for creating any number of P2 repository {@link P2Repository access
 * interfaces}.
 * 
 * @author Elias N Vasylenko
 */
public interface P2RepositoryFactory {
	/**
	 * @return a new {@link P2Repository} implementation
	 */
	P2Repository create();

	/**
	 * @param log
	 *          the log to initialise the repository with
	 * @return a new {@link P2Repository} implementation
	 */
	P2Repository create(Log log);
}