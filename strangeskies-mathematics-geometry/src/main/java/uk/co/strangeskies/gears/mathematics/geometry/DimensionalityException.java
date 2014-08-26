package uk.co.strangeskies.gears.mathematics.geometry;

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
