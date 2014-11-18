package uk.co.strangeskies.mathematics.graph.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.building.GraphBuilder;
import uk.co.strangeskies.mathematics.graph.building.GraphConfigurator;
import uk.co.strangeskies.mathematics.graph.impl.GraphBuilderImpl;
import uk.co.strangeskies.utilities.IdentityComparator;

public class GraphBuilderTest {
	private final Supplier<GraphBuilder> graphBuilderSupplier;

	public GraphBuilderTest() {
		this(() -> new GraphBuilderImpl());
	}

	public GraphBuilderTest(Supplier<GraphBuilder> graphBuilderSupplier) {
		this.graphBuilderSupplier = graphBuilderSupplier;
	}

	protected GraphConfigurator<Object, Object> graph() {
		return graphBuilderSupplier.get().configure();
	}

	@Test
	public Graph<Object, Object> buildTest() {
		return graph().create();
	}

	@Test
	public Graph<String, Object> buildVerticesTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");

		Graph<String, Object> graph = graph().vertices(vertices).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(Collections.emptySet(), graph.edges().keySet());

		return graph;
	}

	@Test
	public Graph<String, Object> buildVerticesExclusionTest() {
		List<String> vertices = Arrays.asList("one", "two", "three", "three");

		Graph<String, Object> graph = graph().vertices(vertices).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(Collections.emptySet(), graph.edges().keySet());

		return graph;
	}

	@Test
	public Graph<String, Object> buildEdgesTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");
		List<EdgeVertices<String>> edges = Arrays.asList(
				EdgeVertices.between("one", "two"),
				EdgeVertices.between("two", "three"));

		Graph<String, Object> graph = graph().vertices(vertices)
				.edgeVertices(edges).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(edges, graph.edges().values());

		return graph;
	}

	@Test
	public Graph<String, Object> buildAutoEdgesTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");
		List<EdgeVertices<String>> edges = Arrays.asList(EdgeVertices.between(
				"one", "two"));

		Graph<String, Object> graph = graph().vertices("one", "two", "three")
				.edgeRule((s1, s2) -> s1.length() == s2.length()).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(edges, graph.edges().values());

		return graph;
	}

	@Test
	public Graph<String, Object> buildUnmodifiableTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");
		List<EdgeVertices<String>> edges = Arrays.asList(
				EdgeVertices.between("one", "two"),
				EdgeVertices.between("two", "three"));

		Graph<String, Object> graph = graph().vertices(vertices)
				.edgeVertices(edges).unmodifiableStructure().create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(edges, graph.edges().values());

		return graph;
	}

	@Test
	public Graph<String, Object> buildDirectedTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");

		Graph<String, Object> graph = graph().vertices(vertices)
				.edgeRule((a, b) -> true).direction(String::compareTo).create();

		Assert.assertEquals(vertices, graph.vertices());

		Assert.assertEquals(Arrays.asList("two", "three"), graph.vertices()
				.outgoingFrom("one"));
		Assert.assertEquals(Collections.emptySet(),
				graph.vertices().incomingTo("one"));

		Assert.assertEquals(Arrays.asList("two"),
				graph.vertices().outgoingFrom("three"));
		Assert.assertEquals(Arrays.asList("one"),
				graph.vertices().incomingTo("three"));

		Assert.assertEquals(Collections.emptySet(),
				graph.vertices().outgoingFrom("three"));
		Assert.assertEquals(Arrays.asList("one", "two"), graph.vertices()
				.incomingTo("three"));

		return graph;
	}

	@Test
	public Graph<String, Object> buildWithComparatorTest() {
		String one = "one";
		String two = "two";
		String three = new String(one);
		List<String> vertices = Arrays.asList(one, two, three);

		Graph<String, Object> graph = graph().vertices(vertices)
				.vertexComparator(new IdentityComparator<>()).edgeRule((a, b) -> true)
				.create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert
				.assertEquals(
						Arrays.asList(EdgeVertices.between(one, two),
								EdgeVertices.between(two, three),
								EdgeVertices.between(three, one)), graph.edges().values());

		return graph;
	}

	@Test
	public Graph<String, Object> buildDirectedWithComparatorTest() {
		String one = "one";
		String two = "two";
		String three = new String(one);
		List<String> vertices = Arrays.asList(one, two, three);

		Graph<String, Object> graph = graph().vertices(vertices)
				.vertexComparator(new IdentityComparator<>()).edgeRule((a, b) -> true)
				.create();

		Assert.assertEquals(vertices, graph.vertices());

		Assert.assertEquals(Arrays.asList(two, three), graph.vertices()
				.outgoingFrom(one));
		Assert.assertEquals(Collections.emptySet(), graph.vertices()
				.incomingTo(one));

		Assert.assertEquals(Arrays.asList(two), graph.vertices()
				.outgoingFrom(three));
		Assert.assertEquals(Arrays.asList(one), graph.vertices().incomingTo(three));

		Assert.assertEquals(Collections.emptySet(),
				graph.vertices().outgoingFrom(three));
		Assert.assertEquals(Arrays.asList(one, two),
				graph.vertices().incomingTo(three));

		return graph;
	}

	@Test
	public Graph<String, String> buildWithEdgeFactoryTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");

		Graph<String, String> graph = graph().vertices(vertices)
				.vertexComparator(new IdentityComparator<>()).edgeRule((a, b) -> true)
				.direction(String::compareTo)
				.edgeFactory(v -> v.getFrom() + " -> " + v.getTo()).create();

		Assert.assertEquals(Arrays.asList("one -> two", "three -> two",
				"one -> three"), graph.edges().keySet());

		return graph;
	}

	// TODO edge vertices from edge object
	// TODO arbitrary edge object and edge vertices combinations
	// TODO simple graph fail with multiple edges between same point
	// TODO generate neighbours test
	// TODO incoming/outgoing generation
	// TODO incoming/outgoing generation with neighbour generation
	// TODO multigraph variants where appropriate
}
