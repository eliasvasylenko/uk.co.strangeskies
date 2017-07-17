package uk.co.strangeskies.observable;

import java.util.function.Predicate;

public class TerminatingObservation<M> extends PassthroughObservation<M, M> {
  private final Observer<? super M> observer;
  private final Predicate<? super M> condition;

  public TerminatingObservation(
      Observable<? extends M> parentObservable,
      Observer<? super M> observer,
      Predicate<? super M> condition) {
    this.observer = observer;
    this.condition = condition;

    passthroughObservation(parentObservable);
  }

  @Override
  public void onObserve() {
    observer.onObserve(this);
  }

  @Override
  public void onNext(M message) {
    if (condition.test(message)) {
      observer.onComplete();
      dispose();
    } else
      observer.onNext(message);
  }

  @Override
  public void onComplete() {
    observer.onComplete();
  }

  @Override
  public void onFail(Throwable t) {
    observer.onFail(t);
  }

  @Override
  public String toString() {
    return getParentObservation() + " -> " + getClass().getSimpleName();
  }
}
