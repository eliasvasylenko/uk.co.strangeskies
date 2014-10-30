package uk.co.strangeskies.mathematics.graph;

public interface EdgeVertices<V> {
	V getFrom();

	V getTo();

	boolean isDirectional();

	public static <V> EdgeVertices<V> between(V from, V to) {
		return new EdgeVertices<V>() {
			@Override
			public V getFrom() {
				return from;
			}

			@Override
			public V getTo() {
				return to;
			}

			@Override
			public boolean isDirectional() {
				return false;
			}

			@Override
			public int hashCode() {
				return isDirectional() ? (from.hashCode() ^ to.hashCode() * 7) : (from
						.hashCode() ^ to.hashCode() * 17);
			}

			@Override
			public boolean equals(Object object) {
				if (!(object instanceof EdgeVertices))
					return false;
				EdgeVertices<?> edge = (EdgeVertices<?>) object;
				return (from.equals(edge.getFrom()) && to.equals(edge.getTo()))
						|| (!isDirectional() && from.equals(edge.getTo()) && to.equals(edge
								.getFrom()));
			}
		};
	}
}
