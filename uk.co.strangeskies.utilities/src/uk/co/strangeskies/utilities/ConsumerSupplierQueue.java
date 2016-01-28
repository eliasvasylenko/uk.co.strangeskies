package uk.co.strangeskies.utilities;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConsumerSupplierQueue<T> implements Consumer<T>, Supplier<T> {
	private final Deque<T> queue = new ArrayDeque<>();

	@Override
	public void accept(T t) {
		synchronized (queue) {
			queue.push(t);
			queue.notifyAll();
		}
	}

	@Override
	public T get() {
		synchronized (queue) {
			try {
				while (queue.isEmpty()) {
					queue.wait();
				}
				return queue.pop();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
