package uk.co.strangeskies.gears.mathematics.graph;

public class EdgeVertices<V> {
	private boolean directional;

	private final V from;

	private final V to;

	public EdgeVertices(V from, V to) {
		this.from = from;
		this.to = to;
	}

	public V getFrom() {
		return from;
	}

	public V getTo() {
		return to;
	}

	public boolean isDirectional() {
		return directional;
	}

	@Override
	public int hashCode() {
		return directional ? (from.hashCode() + to.hashCode())
				: (from.hashCode() * to.hashCode());
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof EdgeVertices))
			return false;
		EdgeVertices<?> edge = (EdgeVertices<?>) object;
		return (from.equals(edge.from) && to.equals(edge.to))
				|| (!directional && from.equals(edge.to) && to.equals(edge.from));
	}
}
