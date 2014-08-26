package uk.co.strangeskies.gears.mathematics.values;

public class FloatValueFactory extends ValueFactory<FloatValue> {
  private static FloatValueFactory floatValueFactory = new FloatValueFactory();

  protected FloatValueFactory() {
  }

  @Override
  public FloatValue create() {
    return new FloatValue();
  }

  public static FloatValueFactory instance() {
    return floatValueFactory;
  }
}
