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
package uk.co.strangeskies.utilities.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface MapDecorator<K, V> extends Map<K, V> {
	Map<K, V> getComponent();

	@Override
	default int size() {
		return getComponent().size();
	}

	@Override
	default boolean isEmpty() {
		return getComponent().isEmpty();
	}

	@Override
	default boolean containsKey(Object key) {
		return getComponent().containsKey(key);
	}

	@Override
	default boolean containsValue(Object value) {
		return getComponent().containsValue(value);
	}

	@Override
	default V get(Object key) {
		return getComponent().get(key);
	}

	@Override
	default V put(K key, V value) {
		return getComponent().put(key, value);
	}

	@Override
	default V remove(Object key) {
		return getComponent().remove(key);
	}

	@Override
	default void putAll(Map<? extends K, ? extends V> m) {
		getComponent().putAll(m);
	}

	@Override
	default void clear() {
		getComponent().clear();
	}

	@Override
	default Set<K> keySet() {
		return getComponent().keySet();
	}

	@Override
	default Collection<V> values() {
		return getComponent().values();
	}

	@Override
	default Set<Map.Entry<K, V>> entrySet() {
		return getComponent().entrySet();
	}
}
