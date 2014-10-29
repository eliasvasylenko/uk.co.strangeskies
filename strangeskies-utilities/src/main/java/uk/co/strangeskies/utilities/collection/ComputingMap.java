package uk.co.strangeskies.utilities.collection;

import java.util.Set;

/**
 * Conceptually, this map behaves much like an ordinary map other than the
 * semantics of putting a key value pair into the map. A ComputingMap is
 * associated with a particular computation - which effectively is equivalent to
 * a function from type {@link K} to type {@link V}. This means that we may
 * simply add a key to the map and have the associated value be calculated and
 * added automatically.
 * 
 * This interface may form the basis of a caching system, for example.
 * 
 * @author eli
 *
 * @param <V>
 * @param <K>
 */
public interface ComputingMap<K, V> {
	/**
	 * For the given key, this method should return the value which has been
	 * computed.
	 * 
	 * This interface, by contract, provides no guarantees that the value for a
	 * previously entered key will still be contained within the map. Despite
	 * this, for keys which have not yet been entered into the map, an invocation
	 * of this method should simply return null, rather than attempting to compute
	 * the given value and then return it.
	 * 
	 * Implementations are, of course, free to provide this guarantee.
	 * 
	 * @param key
	 *          The key object for which to return the mapped computed value.
	 * @return The computed value associated with a given key.
	 */
	V get(K key);

	/**
	 * Enters the key into the map such that a value of type {@link V} will at
	 * some point be computed which will be then returned by any subsequent calls
	 * to {@link ComputingMap#get}.
	 * 
	 * Generally keys should be immutable, and the computation associated with the
	 * map will be expected to produce identical results each time, so in the case
	 * that the key has already been added to the map most implementations should
	 * just return instantly and do no work. Because of this it also makes little
	 * sense to return the previous value for a key entered more than once.
	 * 
	 * @param key
	 *          The key object to be mapped to a new value.
	 * @return True if the associated value was not already contained in the map,
	 *         false otherwise.
	 */
	boolean put(K key);

	/**
	 * This method simply makes sure the value for the given key has been computed
	 * and added to the map, then returns it.
	 * 
	 * @param key
	 * @return
	 */
	default V putGet(K key) {
		V value = get(key);
		while (value == null) {
			put(key);
			value = get(key);
		}
		return value;
	}

	Set<K> keySet();

	/**
	 * Remove the given key and it's associated computed value from the map.
	 * 
	 * @param key
	 *          The key to remove.
	 * @return True if the key was in the map, false otherwise.
	 */
	boolean remove(K key);

	/**
	 * Remove all keys and values from the map.
	 * 
	 * @return True if the map contained keys, false if it was empty.
	 */
	boolean clear();
}