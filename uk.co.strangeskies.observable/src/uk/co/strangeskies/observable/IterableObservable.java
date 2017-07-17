package uk.co.strangeskies.observable;

public class IterableObservable<M> extends HotObservable<M> {
  private final Iterable<? extends M> messages;

  public IterableObservable(Iterable<? extends M> messages) {
    this.messages = messages;
  }

  @Override
  public Observation<M> observe(Observer<? super M> observer) {
    Observation<M> observation = super.observe(observer);
    for (M message : messages) {
      try {
        observer.onNext(message);
      } catch (Throwable t) {
        observer.onFail(t);
        throw t;
      }
    }
    observer.onComplete();
    return observation;
  }
}
