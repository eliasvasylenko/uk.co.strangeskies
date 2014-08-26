package uk.co.strangeskies.gears.utilities.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.Decorator;
import uk.co.strangeskies.gears.utilities.Property;

public class MapDecorator<K, V> extends Decorator<Map<K, V>> implements
		Map<K, V> {
	public MapDecorator(Map<K, V> component) {
		super(component);
	}

	public MapDecorator(Property<Map<K, V>, ? super Map<K, V>> component) {
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
