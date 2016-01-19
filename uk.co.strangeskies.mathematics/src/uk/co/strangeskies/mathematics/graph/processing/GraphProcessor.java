/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.graph.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.graph.Graph;

public class GraphProcessor {
	public class Process<V> implements Iterable<V> {
		private final Graph<V, ?> graph;
		private final Consumer<V> process;

		private final Set<V> processed;
		private final Set<V> ready;

		public Process(Graph<V, ?> graph, Consumer<V> process) {
			this.graph = graph;
			this.process = process;

			processed = graph.createVertexSet();

			ready = graph.vertices().stream()
					.filter(v -> graph.vertices().predecessorsOf(v).isEmpty())
					.collect(Collectors.toCollection(graph::createVertexSet));
		}

		public Set<V> processedVertices() {
			Set<V> processedCopy = graph.createVertexSet();

			synchronized (processed) {
				processedCopy.addAll(processed);
			}

			return processedCopy;
		}

		public Set<V> preparedVertices() {
			Set<V> readyCopy = graph.createVertexSet();

			synchronized (processed) {
				readyCopy.addAll(ready);
			}

			return readyCopy;
		}

		public Set<V> process(V nextVertex) {
			synchronized (processed) {
				if (!ready.remove(nextVertex))
					throw new IllegalStateException();
			}

			process.accept(nextVertex);

			Set<V> readied = graph.createVertexSet();
			synchronized (processed) {
				processed.add(nextVertex);
				readied.addAll(graph.vertices().successorsOf(nextVertex));
				readied.removeAll(ready);
				ready.addAll(readied);
			}

			return readied;
		}

		@Override
		public Iterator<V> iterator() {
			return new Iterator<V>() {
				@Override
				public boolean hasNext() {
					return !ready.isEmpty();
				}

				@Override
				public V next() {
					V processed;

					synchronized (Process.this.processed) {
						processed = ready.iterator().next();
						process(processed);
					}

					return processed;
				}
			};
		}

		public List<V> processEager() {
			List<V> processedList = new ArrayList<>();

			for (V processed : this)
				processedList.add(processed);

			return processedList;
		}

		public List<V> processEagerParallel() {
			// TODO actually parallelise this...
			return processEager();
		}
	}

	public <V, E> Process<V> begin(Graph<V, E> graph) {
		return begin(graph, c -> {});
	}

	public <V, E> Process<V> begin(Graph<V, E> graph, Consumer<V> process) {
		return new Process<>(graph, process);
	}
}
