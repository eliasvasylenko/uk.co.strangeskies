package uk.co.strangeskies.mathematics.graph;

import java.util.Map;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

import uk.co.strangeskies.utilities.Observable;

public interface GraphListeners<V, E> {
	@ProviderType
	interface ChangeSet<V, E> {
		Set<V> verticesAdded();

		Set<V> verticesRemoved();

		Map<E, EdgeVertices<V>> edgesAdded();

		Map<E, EdgeVertices<V>> edgesRemoved();
	}

	@ProviderType
	interface GraphEvent<V, E> {
		Graph<V, E> graph();
	}

	@ProviderType
	interface ChangeEvent<V, E> extends GraphEvent<V, E> {
		ChangeSet<V, E> changes();

		static <V, E> ChangeEvent<V, E> over(Graph<V, E> graph, ChangeSet<V, E> changes) {
			return new ChangeEvent<V, E>() {
				@Override
				public Graph<V, E> graph() {
					return graph;
				}

				@Override
				public ChangeSet<V, E> changes() {
					return changes;
				}
			};
		}
	}

	@ProviderType
	interface EdgeEvent<V, E> extends GraphEvent<V, E> {
		E edge();

		EdgeVertices<V> vertices();

		static <V, E> EdgeEvent<V, E> over(Graph<V, E> graph, E edge, EdgeVertices<V> vertices) {
			return new EdgeEvent<V, E>() {
				@Override
				public Graph<V, E> graph() {
					return graph;
				}

				@Override
				public E edge() {
					return edge;
				}

				@Override
				public EdgeVertices<V> vertices() {
					return vertices;
				}
			};
		}
	}

	@ProviderType
	interface EdgesEvent<V, E> extends GraphEvent<V, E> {
		Map<E, EdgeVertices<V>> edges();

		static <V, E> EdgesEvent<V, E> over(Graph<V, E> graph, Map<E, EdgeVertices<V>> edges) {
			return new EdgesEvent<V, E>() {
				@Override
				public Graph<V, E> graph() {
					return graph;
				}

				@Override
				public Map<E, EdgeVertices<V>> edges() {
					return edges;
				}
			};
		}
	}

	@ProviderType
	interface VertexEvent<V, E> extends GraphEvent<V, E> {
		V vertex();

		static <V, E> VertexEvent<V, E> over(Graph<V, E> graph, V vertex) {
			return new VertexEvent<V, E>() {
				@Override
				public Graph<V, E> graph() {
					return graph;
				}

				@Override
				public V vertex() {
					return vertex;
				}
			};
		}
	}

	@ProviderType
	interface VerticesEvent<V, E> extends GraphEvent<V, E> {
		Set<V> vertices();

		static <V, E> VerticesEvent<V, E> over(Graph<V, E> graph, Set<V> vertices) {
			return new VerticesEvent<V, E>() {
				@Override
				public Graph<V, E> graph() {
					return graph;
				}

				@Override
				public Set<V> vertices() {
					return vertices;
				}
			};
		}
	}

	Observable<ChangeEvent<V, E>> change();

	Observable<EdgeEvent<V, E>> edgeAdded();

	Observable<EdgesEvent<V, E>> edgesAdded();

	Observable<EdgeEvent<V, E>> edgeRemoved();

	Observable<EdgesEvent<V, E>> edgesRemoved();

	Observable<VertexEvent<V, E>> vertexAdded();

	Observable<VerticesEvent<V, E>> verticesAdded();

	Observable<VertexEvent<V, E>> vertexRemoved();

	Observable<VerticesEvent<V, E>> verticesRemoved();
}
