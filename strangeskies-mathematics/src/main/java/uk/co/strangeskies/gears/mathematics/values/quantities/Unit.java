package uk.co.strangeskies.gears.mathematics.values.quantities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.strangeskies.gears.mathematics.values.Fraction;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class Unit {
	private final String name;
	private final String symbol;

	private final Dimension dimension;

	private final List<Map<Unit, Fraction>> equivalencies;

	private final Map<Unit, Double> conversions;

	public Unit(String name, String symbol, Dimension dimension) {
		this.name = name;
		this.symbol = symbol;

		this.dimension = dimension;

		equivalencies = new ArrayList<>();

		conversions = new HashMap<>();
	}

	public Unit(String symbol, Dimension dimension) {
		this(symbol, symbol, dimension);
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	public Dimension getDimension() {
		return dimension;
	}

	public void addEquivalence(Map<Unit, Fraction> equivalence) {
		Dimension.Builder dimensionBuilder = new Dimension.Builder();

		for (Map.Entry<Unit, Fraction> entry : equivalence.entrySet()) {
			dimensionBuilder
					.multiply(entry.getKey().getDimension(), entry.getValue());
		}

		addEquivalence(equivalence, dimensionBuilder.create());
	}

	protected void addEquivalence(Map<Unit, Fraction> equivalence,
			Dimension dimension) {
		if (!this.dimension.equals(dimension)) {
			throw new IllegalArgumentException();
		}

		equivalencies.add(new HashMap<>(equivalence));
	}

	public static <V extends Value<V>> V convert(V value, Unit from, Unit to) {
		return value.multiply(from.conversions.get(to));
	}

	public static <V extends Value<V>> V getConverted(V value, Unit from, Unit to) {
		return value.getMultiplied(from.conversions.get(to));
	}

	public static class Builder implements Factory<Unit> {
		private final Map<Unit, Fraction> equivalence;

		private String name;
		private String symbol;

		public Builder() {
			equivalence = new HashMap<>();
			name = "";
			symbol = "";
		}

		public final Builder name(String name) {
			this.name = name;

			return this;
		}

		public final Builder symbol(String symbol) {
			this.symbol = symbol;

			return this;
		}

		public final Map<Unit, Fraction> getEquivalence() {
			return Collections.unmodifiableMap(equivalence);
		}

		public final Builder multiply(Unit unit, Fraction power) {
			Fraction existingContribution = equivalence.get(unit);
			if (existingContribution != null) {
				power = power.getAdded(existingContribution);
			}

			if (power.equals(0)) {
				equivalence.remove(unit);
			} else {
				equivalence.put(unit, power);
			}

			return this;
		}

		public final Builder multiply(Unit unit, int power) {
			return multiply(unit, new Fraction(power));
		}

		public final Builder multiply(Unit unit) {
			return multiply(unit, 1);
		}

		public final Builder divide(Unit unit, Fraction power) {
			return multiply(unit, power.getNegated());
		}

		public final Builder divide(Unit unit, int power) {
			return multiply(unit, -power);
		}

		public final Builder divide(Unit unit) {
			return multiply(unit, -1);
		}

		public final Dimension getDimension() {
			Dimension.Builder dimensionBuilder = new Dimension.Builder();

			for (Map.Entry<Unit, Fraction> entry : equivalence.entrySet()) {
				dimensionBuilder.multiply(entry.getKey().getDimension(),
						entry.getValue());
			}

			return dimensionBuilder.create();
		}

		public final Unit addAsEquivalenceTo(Unit unit) {
			unit.addEquivalence(getEquivalence(), getDimension());

			return unit;
		}

		@Override
		public final Unit create() {
			return new Unit(name, symbol, getDimension());
		}
	}
}
