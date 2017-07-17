package uk.co.strangeskies.observable;

import java.util.function.Function;

public class MappingObservation<T, M> extends PassthroughObservation<T, M> {
  private final Observer<? super M> observer;
  private final Function<? super T, ? extends M> mapping;

  public MappingObservation(
      Observable<? extends T> parentObservable,
      Observer<? super M> observer,
      Function<? super T, ? extends M> mapping) {
    this.observer = observer;
    this.mapping = mapping;

    passthroughObservation(parentObservable);
  }

  @Override
  public void onObserve() {
    observer.onObserve(this);
  }

  @Override
  public void onNext(T message) {
    observer.onNext(mapping.apply(message));
  }

  @Override
  public void onComplete() {
    observer.onComplete();
  }

  @Override
  public void onFail(Throwable t) {
    observer.onFail(t);
  }
}
