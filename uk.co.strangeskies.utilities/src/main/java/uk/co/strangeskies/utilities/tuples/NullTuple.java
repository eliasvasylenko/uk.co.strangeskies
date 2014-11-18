package uk.co.strangeskies.utilities.tuples;

public final class NullTuple extends Tuple<Void, Tuple<?, ?>> {
  private NullTuple() {
    super(null, null);
  }
}
