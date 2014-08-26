package uk.co.strangeskies.gears.utilities.flowcontrol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FutureMap<K, V> {
	private final HashMap<K, Thread> preparationThreads;
	private final Mapping<K, V> mapping;

	public FutureMap(Mapping<K, V> mapping) {
		preparationThreads = new HashMap<>();
		this.mapping = mapping;
	}

	public void prepare(final K key) {
		synchronized (preparationThreads) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					mapping.prepare(key);
				}
			};
			thread.start();
			preparationThreads.put(key, thread);
			preparationThreads.notifyAll();
		}
	}

	public V get(K key) {
		wait(key);

		return mapping.get(key);
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

	public void reset() {
		synchronized (preparationThreads) {
			waitForAll();
			preparationThreads.clear();
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

	public interface Mapping<K, V> {
		public void prepare(K key);

		public V get(K key);
	}

	public synchronized Set<K> getKeys() {
		return preparationThreads.keySet();
	}
}
