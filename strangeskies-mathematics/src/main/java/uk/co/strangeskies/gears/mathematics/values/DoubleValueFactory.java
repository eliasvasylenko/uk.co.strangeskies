package uk.co.strangeskies.gears.mathematics.values;

public class DoubleValueFactory extends ValueFactory<DoubleValue> {
  private static DoubleValueFactory doubleValueFactory = new DoubleValueFactory();

  protected DoubleValueFactory() {
  }

  @Override
  public DoubleValue create() {
    return new DoubleValue();
  }

  public static DoubleValueFactory instance() {
    return doubleValueFactory;
  }
}
