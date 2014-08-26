package uk.co.strangeskies.gears.mathematics.values.quantities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.values.Fraction;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class Dimension {
	private final Map<QuantityType, /*@ReadOnly*/Fraction> quantityTypepowers;

	protected Dimension(
			Map<QuantityType, /*@ReadOnly*/Fraction> quantityTypepowers) {
		this.quantityTypepowers = Collections.unmodifiableMap(new HashMap<>(
				quantityTypepowers));
	}

	public Map<QuantityType, /*@ReadOnly*/Fraction> getQuantityTypePowers() {
		return quantityTypepowers;
	}

	public Set<QuantityType> getQuantityTypes() {
		return quantityTypepowers.keySet();
	}

	public static class Builder implements Factory<Dimension> {
		private final Map<QuantityType, /*@ReadOnly*/Fraction> quantityTypePowers;

		public Builder() {
			quantityTypePowers = new HashMap<>();
		}

		public final Builder multiply(QuantityType quantityType) {
			return multiply(quantityType, 1);
		}

		public final Builder multiply(QuantityType quantityType, int power) {
			return multiply(quantityType, new Fraction(power));
		}

		public final Builder multiply(QuantityType quantityType, /*@ReadOnly*/
				Fraction power) {
			/*@ReadOnly*/Fraction existingpower = quantityTypePowers
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
			for (Map.Entry<QuantityType, /*@ReadOnly*/Fraction> quantityTypepower : dimension
					.getQuantityTypePowers().entrySet()) {
				multiply(quantityTypepower.getKey(), quantityTypepower.getValue());
			}

			return this;
		}

		public final Builder multiply(Dimension dimension, int power) {
			return multiply(dimension, new Fraction(power));
		}

		public final Builder multiply(Dimension dimension, /*@ReadOnly*/
				Fraction power) {
			for (Map.Entry<QuantityType, /*@ReadOnly*/Fraction> quantityTypepower : dimension
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
