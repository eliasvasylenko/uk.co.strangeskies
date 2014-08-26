package uk.co.strangeskies.gears.mathematics.values;

public class LongValueFactory extends ValueFactory<LongValue> {
  private static LongValueFactory longValueFactory = new LongValueFactory();

  protected LongValueFactory() {
  }

  @Override
  public LongValue create() {
    return new LongValue();
  }

  public static LongValueFactory instance() {
    return longValueFactory;
  }
}
