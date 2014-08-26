package uk.co.strangeskies.gears.utilities.tuples;

public class Quintuple<A, B, C, D, E> extends Tuple<A, Quadruple<B, C, D, E>> {
  public Quintuple(A a, B b, C c, D d, E e) {
    super(a, new Quadruple<>(b, c, d, e));
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

  public E get4() {
    return getTail().getTail().getTail().getTail().getHead();
  }
}
