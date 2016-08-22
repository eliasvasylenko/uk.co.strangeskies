/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.fx.
 *
 * uk.co.strangeskies.fx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.fx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.fx;

import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

/**
 * A type of contribution for root items in a {@link ModularTreeView}.
 * 
 * @author Elias N Vasylenko
 */
public interface TreeContribution<T> {
	default TypeToken<T> getDataType() {
		return TypeToken.over(getClass()).resolveSupertypeParameters(TreeChildContribution.class)
				.resolveTypeArgument(new TypeParameter<T>() {}).infer();
	}

	/**
	 * Determine whether the contribution should be applied to the given data
	 * item. This method will only be invoked <em>after</em>
	 * {@link #getDataType()} has checked against the exact item type.
	 * 
	 * @param data
	 *          a data item in the tree
	 * @return true if the contribution is applicable, false otherwise
	 */
	boolean appliesTo(T data);
}
