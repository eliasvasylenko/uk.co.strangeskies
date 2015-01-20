/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.graph;

public interface EdgeVertices<V> {
	V getFrom();

	V getTo();

	static <V> EdgeVertices<V> between(V from, V to) {
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
			public int hashCode() {
				return from.hashCode() ^ to.hashCode() * 7;
			}

			@Override
			public boolean equals(Object object) {
				if (!(object instanceof EdgeVertices))
					return false;
				EdgeVertices<?> edge = (EdgeVertices<?>) object;
				return (from.equals(edge.getFrom()) && to.equals(edge.getTo()))
						|| (from.equals(edge.getTo()) && to.equals(edge.getFrom()));
			}
		};
	}
}
