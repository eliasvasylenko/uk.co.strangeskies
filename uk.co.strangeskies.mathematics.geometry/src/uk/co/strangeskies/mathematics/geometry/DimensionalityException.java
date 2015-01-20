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
package uk.co.strangeskies.mathematics.geometry;

public class DimensionalityException extends Exception {
	/**
   * 
   */
	private static final long serialVersionUID = -711937026976919911L;

	protected DimensionalityException(int dimensionsA, int dimensionsB) {
		super("The dimensionality of " + dimensionsA
				+ " is inconsistent with that of " + dimensionsB + ".");
	}

	protected DimensionalityException(int dimensions) {
		super("The dimensionality of " + dimensions + " is invalid.");
	}

	public static void checkEquivalence(int dimensionsA, int dimensionsB)
			throws DimensionalityException {
		checkValid(dimensionsA);
		checkValid(dimensionsB);
		if (dimensionsA != dimensionsB) {
			throw new DimensionalityException(dimensionsA, dimensionsB);
		}
	}

	public static void checkValid(int dimensions) throws DimensionalityException {
		if (dimensions <= 0) {
			throw new DimensionalityException(dimensions);
		}
	}
}
