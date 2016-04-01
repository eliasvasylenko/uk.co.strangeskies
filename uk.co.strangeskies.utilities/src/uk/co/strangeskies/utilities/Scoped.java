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
package uk.co.strangeskies.utilities;

import java.util.Optional;

/**
 * A general interface describing a system with a hierarchical scope for
 * visibility of the contents of that system. Child scopes have visibility over
 * everything visible to their parents, but parents do not have visibility over
 * the contents of their children.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the self bounding of the scoped object
 */
public interface Scoped<T extends Self<T>> extends Self<T> {
	/**
	 * @return the parent scope if one exists, otherwise null
	 */
	Optional<T> getParentScope();

	/**
	 * Collapse this scope into its parent. This will result in the contents of
	 * this scope becoming visible to the parent scope, and all the rest of that
	 * scope's children.
	 * 
	 * @throws NullPointerException
	 *           if the parent scope doesn't exist
	 */
	void collapseIntoParentScope();

	/**
	 * @return a new child scope, with the receiver as its parent
	 */
	T nestChildScope();
}
