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
package uk.co.strangeskies.mathematics.values.quantities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.mathematics.values.Fraction;
import uk.co.strangeskies.utilities.factory.Factory;

public class Dimension {
	private final Map<QuantityType, /**/Fraction> quantityTypepowers;

	protected Dimension(
			Map<QuantityType, /**/Fraction> quantityTypepowers) {
		this.quantityTypepowers = Collections.unmodifiableMap(new HashMap<>(
				quantityTypepowers));
	}

	public Map<QuantityType, /**/Fraction> getQuantityTypePowers() {
		return quantityTypepowers;
	}

	public Set<QuantityType> getQuantityTypes() {
		return quantityTypepowers.keySet();
	}

	public static class Builder implements Factory<Dimension> {
		private final Map<QuantityType, /**/Fraction> quantityTypePowers;

		public Builder() {
			quantityTypePowers = new HashMap<>();
		}

		public final Builder multiply(QuantityType quantityType) {
			return multiply(quantityType, 1);
		}

		public final Builder multiply(QuantityType quantityType, int power) {
			return multiply(quantityType, new Fraction(power));
		}

		public final Builder multiply(QuantityType quantityType, /**/
				Fraction power) {
			/**/Fraction existingpower = quantityTypePowers
					.get(quantityType);
			if (existingpower != null) {
				power = power.getAdded(existingpower);
			}

			if (power.equals(0)) {
				quantityTypePowers.remove(quantityType);
			} else {
				quantityTypePowers.put(quantityType, power);
			}

			return this;
		}

		public final Builder multiply(Dimension dimension) {
			for (Map.Entry<QuantityType, /**/Fraction> quantityTypepower : dimension
					.getQuantityTypePowers().entrySet()) {
				multiply(quantityTypepower.getKey(), quantityTypepower.getValue());
			}

			return this;
		}

		public final Builder multiply(Dimension dimension, int power) {
			return multiply(dimension, new Fraction(power));
		}

		public final Builder multiply(Dimension dimension, /**/
				Fraction power) {
			for (Map.Entry<QuantityType, /**/Fraction> quantityTypepower : dimension
					.getQuantityTypePowers().entrySet()) {
				multiply(quantityTypepower.getKey(), quantityTypepower.getValue()
						.getMultiplied(power));
			}

			return this;
		}

		@Override
		public Dimension create() {
			return new Dimension(quantityTypePowers);
		}
	}
}
