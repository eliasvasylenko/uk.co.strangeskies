package uk.co.strangeskies.gears.mathematics.graph;

import uk.co.strangeskies.gears.mathematics.graph.building.GraphBuilder;
import uk.co.strangeskies.gears.utilities.IdentityProperty;
import uk.co.strangeskies.gears.utilities.SimpleProperty;

public class GraphTest {
	@SuppressWarnings("unused")
	public static void main(String... args) {
		GraphBuilder build = (GraphBuilder) new Object();

		Graph<String, Double> graph = build.configure()
				.vertices("node", "noooode", "wat").edgeFactory((o, p) -> 1d)
				.edgeWeight(e -> e, false).create();

		Graph<String, Integer> graph2 = build.configure().unmodifiableEdges()
				.<String> vertices().edgeFactory((o, p) -> o.length() - p.length())
				.edgeWeight(e -> (double) e, false).create();

		@SuppressWarnings("unchecked")
		Graph<?, ? extends SimpleProperty<String>> graph3 = build.configure()
				.vertices(1d, 2, "", 4, 5, 6).unmodifiableStructure()
				.edgeFactory(() -> new IdentityProperty<>("edge"))
				.edgeWeight(e -> (double) e.get().length(), true).create();

		Graph<String, String> graph4 = graph3.transform()
				.vertices(v -> v.toString()).edges(e -> e.get()).create();
	}
}
