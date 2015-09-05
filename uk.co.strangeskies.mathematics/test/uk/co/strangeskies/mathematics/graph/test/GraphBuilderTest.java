/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.mathematics.graph.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Assert;

import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.building.GraphBuilder;
import uk.co.strangeskies.mathematics.graph.building.GraphConfigurator;
import uk.co.strangeskies.mathematics.graph.impl.GraphBuilderImpl;

public class GraphBuilderTest {
	private final Supplier<GraphBuilder> graphBuilderSupplier;

	public GraphBuilderTest() {
		graphBuilderSupplier = GraphBuilderImpl::new;
	}

	/*
	 * public GraphBuilderTest(Supplier<GraphBuilder> graphBuilderSupplier) {
	 * this.graphBuilderSupplier = graphBuilderSupplier; }
	 */

	protected GraphConfigurator<Object, Object> graph() {
		return graphBuilderSupplier.get().configure();
	}

	// @Test
	public void buildTest() {
		graph().create();
	}

	// @Test
	public void buildVerticesTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");

		Graph<String, Object> graph = graph().vertices(vertices).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(Collections.emptySet(), graph.edges().keySet());
	}

	// @Test
	public void buildVerticesExclusionTest() {
		List<String> vertices = Arrays.asList("one", "two", "three", "three");

		Graph<String, Object> graph = graph().vertices(vertices).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(Collections.emptySet(), graph.edges().keySet());
	}

	// @Test
	public void buildEdgesTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");
		List<EdgeVertices<String>> edges = Arrays.asList(
				EdgeVertices.between("one", "two"),
				EdgeVertices.between("two", "three"));

		Graph<String, Object> graph = graph().vertices(vertices).edge("one", "two")
				.create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(edges, graph.edges().values());
	}

	// @Test
	public void buildAutoEdgesTest() {
		Set<String> vertices = new HashSet<>(Arrays.asList("one", "two", "three"));
		Set<EdgeVertices<String>> edges = new HashSet<>(Arrays.asList(EdgeVertices
				.between("one", "two")));

		/*
		Graph<String, Object> graph = graph().vertices("one", "two", "three")
				.edgeGenerationRule((s1, s2) -> s1.length() == s2.length()).create();

		Assert.assertEquals(graph.vertices(), vertices);
		Assert.assertEquals(graph.edges().values(), edges);*/
	}

	// @Test
	public void buildUnmodifiableTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");
		List<EdgeVertices<String>> edges = Arrays.asList(
				EdgeVertices.between("one", "two"),
				EdgeVertices.between("two", "three"));

		Graph<String, Object> graph = graph().vertices(vertices).edges(edges)
				.readOnly().create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(edges, graph.edges().values());
	}

	// @Test
	public void buildDirectedTest() {
		Set<String> vertices = new HashSet<>(Arrays.asList("one", "two", "three"));

		/*
		Graph<String, Object> graph = graph().vertices(vertices)
				.edgeGenerationRule((a, b) -> true).direction(String::compareTo)
				.create();

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
				.incomingTo("three"));*/
	}

	// @Test
	public void buildWithComparatorTest() {
		String one = "one";
		String two = "two";
		String three = new String(one);
		List<String> vertices = Arrays.asList(one, two, three);

		/*
		Graph<String, Object> graph = graph().vertices(vertices)
				.vertexComparator(new IdentityComparator<>())
				.edgeGenerationRule((a, b) -> true).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert
				.assertEquals(
						Arrays.asList(EdgeVertices.between(one, two),
								EdgeVertices.between(two, three),
								EdgeVertices.between(three, one)), graph.edges().values());*/
	}

	// @Test
	public void buildDirectedWithComparatorTest() {
		String one = "one";
		String two = "two";
		String three = new String(one);
		List<String> vertices = Arrays.asList(one, two, three);

		/*
		Graph<String, Object> graph = graph().vertices(vertices)
				.vertexComparator(new IdentityComparator<>())
				.edgeGenerationRule((a, b) -> true).create();

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
				graph.vertices().incomingTo(three));*/
	}

	// @Test
	public void buildWithEdgeFactoryTest() {
		List<String> vertices = Arrays.asList("one", "two", "three");

		/*
		Graph<String, String> graph = graph().vertices(vertices)
				.vertexComparator(new IdentityComparator<>())
				.edgeGenerationRule((a, b) -> true).direction(String::compareTo)
				.edgeFactory(v -> v.getFrom() + " -> " + v.getTo()).create();

		Assert.assertEquals(Arrays.asList("one -> two", "three -> two",
				"one -> three"), graph.edges().keySet());*/
	}

	// TODO edge vertices from edge object
	// TODO arbitrary edge object and edge vertices combinations
	// TODO simple graph fail with multiple edges between same point
	// TODO generate neighbours test
	// TODO incoming/outgoing generation
	// TODO incoming/outgoing generation with neighbour generation
	// TODO multigraph variants where appropriate
}
