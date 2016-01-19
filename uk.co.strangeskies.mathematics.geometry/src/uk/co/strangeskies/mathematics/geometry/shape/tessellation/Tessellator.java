/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.geometry.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry.shape.tessellation;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.SimplePolygon;
import uk.co.strangeskies.mathematics.geometry.shape.mesh.Mesh;
import uk.co.strangeskies.mathematics.geometry.shape.mesh.Mesh.MeshingScheme;
import uk.co.strangeskies.mathematics.values.Value;

public interface Tessellator {
	public <V extends Value<V>> Mesh<Vector2<V>> getTessellationMesh(
			SimplePolygon<?, V> polygon, MeshingScheme scheme, int runLimit,
			boolean restrictToConvex);
}
