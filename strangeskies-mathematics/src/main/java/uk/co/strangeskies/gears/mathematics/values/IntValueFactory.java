package uk.co.strangeskies.gears.mathematics.values;

public class IntValueFactory extends ValueFactory<IntValue> {
  private static IntValueFactory intValueFactory = new IntValueFactory();

  protected IntValueFactory() {
  }

  @Override
  public IntValue create() {
    return new IntValue();
  }

  public static IntValueFactory instance() {
    return intValueFactory;
  }
}
