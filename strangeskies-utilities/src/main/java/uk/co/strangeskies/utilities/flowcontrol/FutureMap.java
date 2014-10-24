package uk.co.strangeskies.utilities.flowcontrol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.utilities.collection.ComputingMap;

public class FutureMap<K, V> implements ComputingMap<K, V> {
	private final HashMap<K, Thread> preparationThreads;
	private final Consumer<K> prepare;
	private final Function<K, V> mapping;

	public FutureMap(Consumer<K> prepare, Function<K, V> mapping) {
		preparationThreads = new HashMap<>();
		this.prepare = prepare;
		this.mapping = mapping;
	}

	public FutureMap(Function<K, V> function) {
		this(function, HashMap::new);
	}

	public FutureMap(Function<K, V> function, Supplier<Map<K, V>> valueMap) {
		preparationThreads = new HashMap<>();

		Map<K, V> values = valueMap.get();
		prepare = key -> {
			V value = function.apply(key);

			synchronized (preparationThreads) {
				if (preparationThreads.containsKey(key))
					values.put(key, value);
			}
		};
		mapping = k -> values.get(k);
	}

	public boolean put(final K key) {
		synchronized (preparationThreads) {
			if (preparationThreads.containsKey(key))
				return false;

			Thread thread = new Thread() {
				@Override
				public void run() {
					prepare.accept(key);
				}
			};
			thread.start();
			preparationThreads.put(key, thread);
			preparationThreads.notifyAll();

			return true;
		}
	}

	public V get(K key) {
		wait(key);

		return mapping.apply(key);
	}

	@Override
	public boolean remove(K key) {
		synchronized (preparationThreads) {
			if (!preparationThreads.containsKey(key))
				return false;

			wait(key);
			preparationThreads.remove(key).interrupt();

			return true;
		}
	}

	private void wait(K key) {
		Thread thread;
		synchronized (preparationThreads) {
			while (!preparationThreads.containsKey(key)) {
				try {
					preparationThreads.wait();
					preparationThreads.notifyAll();
				} catch (InterruptedException e) {
				}
			}
			thread = preparationThreads.get(key);
		}
		while (true) {
			try {
				thread.join();
				break;
			} catch (InterruptedException e) {
			}
		}
	}

	public boolean clear() {
		synchronized (preparationThreads) {
			if (preparationThreads.isEmpty())
				return false;

			waitForAll();
			preparationThreads.clear();

			return true;
		}
	}

	public void waitForAll() {
		Set<K> keys = new HashSet<>();
		synchronized (preparationThreads) {
			keys.addAll(preparationThreads.keySet());
		}
		for (K key : keys) {
			wait(key);
		}
	}

	public synchronized Set<K> getKeys() {
		return preparationThreads.keySet();
	}
}
