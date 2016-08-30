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

import java.util.List;
import java.util.Optional;

import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public interface TreeItemData<T> {
	TypedObject<T> typedData();

	default T data() {
		return typedData().getObject();
	}

	default TypeToken<?> type() {
		return typedData().getType();
	}

	Optional<TreeItemData<?>> parent();

	/**
	 * Get all the contributions which should be applied to a tree item, in order
	 * from most to least specific.
	 * 
	 * @return the contributions which apply to this tree item
	 */
	List<TreeContribution<? super T>> contributions();

	<U extends TreeContribution<? super T>> List<U> contributions(TypeToken<U> type);
}
