package uk.co.strangeskies.observable;

import java.lang.ref.Reference;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReferenceOwnedObservation<O, M> extends PassthroughObservation<M, OwnedMessage<O, M>> {
  private final Observer<? super OwnedMessage<O, M>> observer;
  private final Reference<O> ownerReference;

  public ReferenceOwnedObservation(
      Observable<? extends M> parentObservable,
      Observer<? super OwnedMessage<O, M>> observer,
      O owner,
      Function<O, Reference<O>> referenceFunction) {
    this.observer = observer;
    this.ownerReference = referenceFunction.apply(owner);

    passthroughObservation(parentObservable);
  }

  public void withOwner(Consumer<O> action) {
    O owner = ownerReference.get();
    if (owner != null) {
      action.accept(owner);
    } else {
      dispose();
    }
  }

  @Override
  public void onObserve() {
    observer.onObserve(this);
  }

  @Override
  public void onNext(M message) {
    withOwner(o -> observer.onNext(new OwnedMessage<O, M>() {
      @Override
      public O owner() {
        return o;
      }

      @Override
      public M message() {
        return message;
      }
    }));
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
