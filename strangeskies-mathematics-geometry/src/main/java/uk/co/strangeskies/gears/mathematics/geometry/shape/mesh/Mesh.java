package uk.co.strangeskies.gears.mathematics.geometry.shape.mesh;

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;

public interface Mesh<T extends Vector<?, ?>> {
	public enum MeshingScheme {
		TriangleList, Fan, Strip, AlternatingStrip;
	}

	public List<T> getVertices();

	public MeshingScheme getMeshingScheme();

	public Set<MeshFragment> getFragments();
}
