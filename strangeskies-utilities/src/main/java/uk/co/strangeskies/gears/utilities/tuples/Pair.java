package uk.co.strangeskies.gears.utilities.tuples;

public class Pair<L, R> extends Tuple<L, Unit<R>> {
  public Pair(L left, R right) {
    super(left, new Unit<>(right));
  }

  public L get0() {
    return getHead();
  }

  public L getLeft() {
    return getHead();
  }

  public R get1() {
    return getTail().getHead();
  }

  public R getRight() {
    return getTail().getHead();
  }
}
