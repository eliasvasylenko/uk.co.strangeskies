package uk.co.strangeskies.gears.utilities.tuples;

public class Triple<A, B, C> extends Tuple<A, Pair<B, C>> {
  public Triple(A a, B b, C c) {
    super(a, new Pair<>(b, c));
  }

  public A get0() {
    return getHead();
  }

  public B get1() {
    return getTail().getHead();
  }

  public C get2() {
    return getTail().getTail().getHead();
  }
}
