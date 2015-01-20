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
package uk.co.strangeskies.mathematics.values.quantities;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import uk.co.strangeskies.mathematics.values.Fraction;
import uk.co.strangeskies.utilities.function.collection.ListTransformOnceView;

public class QuantityType {
	private final String name;
	private final String symbol;

	private final List<Map<QuantityType, Fraction>> equivalencies;

	public QuantityType(String name, String symbol,
			List<Map<QuantityType, Fraction>> equivalencies) {
		this.name = name;
		this.symbol = symbol;

		this.equivalencies = equivalencies;
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	public List<Map<QuantityType, Fraction>> getEquivalencies() {
		return new ListTransformOnceView<>(equivalencies,
				m -> Collections.unmodifiableMap(m));
	}

	public Dimension getDimension() {
		return new Dimension.Builder().multiply(this).create();
	}
}
