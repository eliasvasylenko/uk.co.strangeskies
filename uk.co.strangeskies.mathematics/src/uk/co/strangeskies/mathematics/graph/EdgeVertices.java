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
package uk.co.strangeskies.mathematics.graph;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
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
