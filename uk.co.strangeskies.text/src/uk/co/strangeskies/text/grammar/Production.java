package uk.co.strangeskies.text.grammar;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Production<T> {
  public static <T> Production<T> produce(Symbol<T> symbol) {
    return null;
  }

  public Production<Stream<T>> repeat() {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<Stream<T>> repeat(int minimum) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<Stream<T>> repeat(int minimum, int maximum) {
    // TODO Auto-generated method stub
    return null;
  }

  public <U> Production<U> map(Function<T, U> in, Function<U, T> out) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> prepend(String terminal) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> append(String terminal) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> prepend(Symbol<?> symbol) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> append(Symbol<?> symbol) {
    // TODO Auto-generated method stub
    return null;
  }

  public <U, R> Production<R> append(
      Symbol<U> symbol,
      BiFunction<? super T, ? super U, ? extends R> in,
      Function<? super R, ? extends T> outT,
      Function<? super R, ? extends U> outU) {
    // TODO Auto-generated method stub
    return null;
  }

  public <U, R> Production<R> append(
      Production<U> symbol,
      BiFunction<? super T, ? super U, ? extends R> in,
      Function<? super R, ? extends T> outT,
      Function<? super R, ? extends U> outU) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> check(Predicate<T> test) {
    // TODO Auto-generated method stub
    return null;
  }
}
