package uk.co.strangeskies.observable;

import java.lang.ref.Reference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReferenceObservation<M> extends PassthroughObservation<M, M> {
  private final Supplier<Observer<? super M>> observerReference;

  public ReferenceObservation(
      Observable<? extends M> parentObservable,
      Observer<? super M> observer,
      Function<Observer<? super M>, Reference<Observer<? super M>>> referenceFunction) {
    observerReference = referenceFunction.apply(observer)::get;

    passthroughObservation(parentObservable);
  }

  public void withObserver(Consumer<Observer<? super M>> action) {
    Observer<? super M> observer = observerReference.get();
    if (observer != null) {
      action.accept(observer);
    } else {
      dispose();
    }
  }

  @Override
  public void onObserve() {
    withObserver(o -> o.onObserve(this));
  }

  @Override
  public void onNext(M message) {
    withObserver(o -> o.onNext(message));
  }

  @Override
  public void onComplete() {
    withObserver(o -> o.onComplete());
  }

  @Override
  public void onFail(Throwable t) {
    withObserver(o -> o.onFail(t));
  }
}
