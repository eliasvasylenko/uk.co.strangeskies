/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;

import uk.co.strangeskies.utilities.Factory;

public class MultiTreeMap<K, V, C extends Collection<V>> extends TreeMap<K, C>
		implements MultiMap<K, V, C> {
	private static final long serialVersionUID = 1L;

	private final Factory<C> collectionFactory;

	public MultiTreeMap(Factory<C> collectionFactory) {
		this.collectionFactory = collectionFactory;
	}

	public MultiTreeMap(Comparator<? super K> comparator,
			Factory<C> collectionFactory) {
		super(comparator);
		this.collectionFactory = collectionFactory;
	}

	@Override
	public C createCollection() {
		return collectionFactory.create();
	}
}
