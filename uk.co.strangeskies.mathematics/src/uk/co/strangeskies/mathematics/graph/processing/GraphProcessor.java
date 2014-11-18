package uk.co.strangeskies.mathematics.graph.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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

			processed = new TreeSet<>(graph.vertices().comparator());

			ready = graph
					.vertices()
					.stream()
					.filter(v -> graph.vertices().incomingTo(v).isEmpty())
					.collect(
							Collectors.toCollection(() -> new TreeSet<>(graph.vertices()
									.comparator())));
		}

		public Set<V> processedVertices() {
			Set<V> processedCopy = new TreeSet<>(graph.vertices().comparator());

			synchronized (processed) {
				processedCopy.addAll(processed);
			}

			return processedCopy;
		}

		public Set<V> preparedVertices() {
			Set<V> readyCopy = new TreeSet<>(graph.vertices().comparator());

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

			Set<V> readied = new TreeSet<>(graph.vertices().comparator());
			synchronized (processed) {
				processed.add(nextVertex);
				readied.addAll(graph.vertices().outgoingFrom(nextVertex));
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
		return begin(graph, c -> {
		});
	}

	public <V, E> Process<V> begin(Graph<V, E> graph, Consumer<V> process) {
		return new Process<>(graph, process);
	}
}
