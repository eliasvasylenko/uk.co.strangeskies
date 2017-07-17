package uk.co.strangeskies.observable;

public interface Observation<M> extends Disposable {
  default void request(long count) {
    throw new MissingBackpressureException(this);
  }

  default void requestUnbounded() {}
}
