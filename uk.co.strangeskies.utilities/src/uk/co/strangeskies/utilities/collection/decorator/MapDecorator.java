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
package uk.co.strangeskies.utilities.collection.decorator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import uk.co.strangeskies.utilities.Decorator;

public class MapDecorator<K, V> extends Decorator<Map<K, V>> implements
		Map<K, V> {
	public MapDecorator(Map<K, V> component) {
		super(component);
	}

	public MapDecorator(Supplier<Map<K, V>> component) {
		super(component);
	}

	@Override
	public int size() {
		return getComponent().size();
	}

	@Override
	public boolean isEmpty() {
		return getComponent().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return getComponent().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return getComponent().containsValue(value);
	}

	@Override
	public V get(Object key) {
		return getComponent().get(key);
	}

	@Override
	public V put(K key, V value) {
		return getComponent().put(key, value);
	}

	@Override
	public V remove(Object key) {
		return getComponent().remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		getComponent().putAll(m);
	}

	@Override
	public void clear() {
		getComponent().clear();
	}

	@Override
	public Set<K> keySet() {
		return getComponent().keySet();
	}

	@Override
	public Collection<V> values() {
		return getComponent().values();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return getComponent().entrySet();
	}
}
