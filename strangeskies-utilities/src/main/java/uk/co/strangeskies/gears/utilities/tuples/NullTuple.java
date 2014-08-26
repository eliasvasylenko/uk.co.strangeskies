package uk.co.strangeskies.gears.utilities.tuples;

public final class NullTuple extends Tuple<Void, Tuple<?, ?>> {
  private NullTuple() {
    super(null, null);
  }
}
