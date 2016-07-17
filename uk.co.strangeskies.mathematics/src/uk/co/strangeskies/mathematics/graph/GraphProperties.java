/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;

public interface GraphProperties extends Properties<GraphProperties> {
	Localized<String> alreadyConfigured(Object item);

	String edgeMap();

	String edgeVertices();

	String edgeFactory();

	String edgeMultiFactory();

	String edgeWeight();

	String edgeEquality();

	String vertexEquality();

	String direction();

	String directionFunction();
}
