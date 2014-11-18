package uk.co.strangeskies.utilities.tuples;

public class Unit<H> extends Tuple<H, NullTuple> {
  public Unit(H head) {
    super(head);
  }
}
