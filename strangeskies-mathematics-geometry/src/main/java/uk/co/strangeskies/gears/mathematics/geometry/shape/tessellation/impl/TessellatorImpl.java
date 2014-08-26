package uk.co.strangeskies.gears.mathematics.geometry.shape.tessellation.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.shape.SimplePolygon;
import uk.co.strangeskies.gears.mathematics.geometry.shape.Triangle;
import uk.co.strangeskies.gears.mathematics.geometry.shape.SimplePolygon.WindingDirection;
import uk.co.strangeskies.gears.mathematics.geometry.shape.impl.Line2Impl;
import uk.co.strangeskies.gears.mathematics.geometry.shape.impl.TriangleImpl;
import uk.co.strangeskies.gears.mathematics.geometry.shape.mesh.Mesh;
import uk.co.strangeskies.gears.mathematics.geometry.shape.mesh.MeshFragment;
import uk.co.strangeskies.gears.mathematics.geometry.shape.mesh.Mesh.MeshingScheme;
import uk.co.strangeskies.gears.mathematics.geometry.shape.tessellation.Tessellator;
import uk.co.strangeskies.gears.mathematics.values.Value;

public class TessellatorImpl implements Tessellator {
	@Override
	public <V extends Value<V>> Mesh<Vector2<V>> getTessellationMesh(
			SimplePolygon<?, V> polygon, final MeshingScheme scheme, int runLimit,
			boolean restrictToConvex) {
		final List<Vector2<V>> vertices = polygon.getVertices();
		final Set<MeshFragment> meshFragments = new HashSet<>();

		// output list
		Mesh<Vector2<V>> outputMesh = new Mesh<Vector2<V>>() {
			@Override
			public List<Vector2<V>> getVertices() {
				return vertices;
			}

			@Override
			public MeshingScheme getMeshingScheme() {
				return scheme;
			}

			@Override
			public Set<MeshFragment> getFragments() {
				return meshFragments;
			}
		};

		List</*@Immutable*/Integer> remainingIndices = new ArrayList<>();
		for (int i = 0; i < vertices.size(); i++)
			remainingIndices.add(i);

		int earIndex;
		int nextIndex;
		int lastValidEarIndex;

		earIndex = lastValidEarIndex = 0;
		nextIndex = earIndex + 1;

		List<Integer> tessellationIndices = new ArrayList<>();
		while (remainingIndices.size() > 2) {
			if (isValidEar(vertices, remainingIndices, earIndex)) {
				tessellationIndices.add(earIndex);
				do {
					tessellationIndices.add(nextIndex);
					remainingIndices.remove(nextIndex);
					earIndex = nextIndex - 1;
					if (nextIndex == remainingIndices.size())
						nextIndex = 0;
				} while (remainingIndices.size() > 2
						&& isValidEar(vertices, remainingIndices, earIndex)
						&& (tessellationIndices.size() < runLimit + 1 || runLimit == 0));
				tessellationIndices.add(nextIndex);

				final List<Integer> meshFragmentIndeces = new ArrayList<>(
						tessellationIndices);
				tessellationIndices.clear();
				meshFragments.add(new MeshFragment() {
					@Override
					public List<Integer> getIndices() {
						return meshFragmentIndeces;
					}
				});

				lastValidEarIndex = earIndex;
			}

			earIndex++;
			if (earIndex == remainingIndices.size())
				earIndex = 0;

			if (earIndex == lastValidEarIndex) {
				System.out.println("failed to tesselate @ " + lastValidEarIndex
						+ " with " + remainingIndices.size() + " vertices remaining");
				for (Integer index : remainingIndices) {
					System.out.println(vertices.get(index));
				}

				return outputMesh;
			}
			nextIndex = earIndex + 1;
			if (nextIndex == remainingIndices.size())
				nextIndex = 0;
		}

		return outputMesh;
	}

	public <V extends Value<V>> boolean isValidEar(
	/*@ReadOnly*/List</*@Immutable*/Vector2<V>> vertices,
	/*@ReadOnly*/List</*@Immutable*/Integer> remainingIndices, int earIndex)
	/*@ReadOnly*/{
		int indicesLeft = remainingIndices.size();

		// if not even triangle
		if (indicesLeft < 3)
			return false;

		// ear triangle
		/*@ReadOnly*/Vector2<V> a = vertices.get(remainingIndices.get(earIndex));
		if (++earIndex == indicesLeft)
			earIndex = 0;
		/*@ReadOnly*/Vector2<V> b = vertices.get(remainingIndices.get(earIndex));
		if (++earIndex == indicesLeft)
			earIndex = 0;
		/*@ReadOnly*/Vector2<V> c = vertices.get(remainingIndices.get(earIndex));

		Triangle<V> triangle = new TriangleImpl<V>(a, b, c);

		// if ear is concave then reject as invalid
		if (triangle.getWindingDirection() == WindingDirection.Anticlockwise)
			return false;

		// if ear intersects with any other geometry it is invalid
		Vector2<V> previousVertex = vertices.get(vertices.size() - 1);
		for (Vector2<V> vertex : vertices) {
			// if a vertex is inside the ear
			if (triangle.contains(vertex))
				return false;

			// if a line crosses through the ear it is invalid
			if (triangle.intersects(new Line2Impl<V>(previousVertex, vertex)))
				return false;

			/* Check if any point(s) 'n' overlap b, then for each check if the angle
			* swept about b through the solid portion (by winding) between a and c
			* intersects the angle swept about n through the solid portion between
			* its own neighbouring points. If intersection found then fail.
			*
			* It's worth noting that In this case a valid triangulation may still
			* fail, but it doesn't matter as the polygon will be reduced elsewhere
			* until an ear at b is made passable. Also the alternative, to more
			* thoroughly check validity here, is way too overcomplicated...
			*/
			// TO DO (very rare case, only for *very* redundant empty geometry)

			previousVertex = vertex;
		}

		return true;
	}
}
