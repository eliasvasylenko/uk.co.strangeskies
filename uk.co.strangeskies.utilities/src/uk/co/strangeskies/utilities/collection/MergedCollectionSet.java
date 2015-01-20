/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MergedCollectionSet<T> extends AbstractSet<T> {
	private final Set<? extends Collection<? extends T>> backingCollections;

	public MergedCollectionSet(
			Set<? extends Collection<? extends T>> backingCollections) {
		this.backingCollections = backingCollections;
	}

	@Override
	public Iterator<T> iterator() {
		return getSnapshot().iterator();
	}

	@Override
	public final int size() {
		return getSnapshot().size();
	}

	public Set<T> getSnapshot() {
		HashSet<T> set = new HashSet<>();

		for (Collection<? extends T> collection : backingCollections) {
			set.addAll(collection);
		}

		return set;
	}
}
