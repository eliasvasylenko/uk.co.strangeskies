package uk.co.strangeskies.gears.utilities.tuples;

public class Quadruple<A, B, C, D> extends Tuple<A, Triple<B, C, D>> {
  public Quadruple(A a, B b, C c, D d) {
    super(a, new Triple<>(b, c, d));
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

  public D get3() {
    return getTail().getTail().getTail().getHead();
  }
}
