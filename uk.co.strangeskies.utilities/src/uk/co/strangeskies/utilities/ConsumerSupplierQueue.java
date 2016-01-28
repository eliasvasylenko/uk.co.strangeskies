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
