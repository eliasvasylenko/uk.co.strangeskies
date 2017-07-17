package uk.co.strangeskies.observable;

public class HotObservation<M> implements Observation<M> {
  private final HotObservable<M> observable;
  private final Observer<? super M> observer;
  private boolean disposed;

  public HotObservation(HotObservable<M> observable, Observer<? super M> observer) {
    this.observable = observable;
    this.observer = observer;
    observer.onObserve(this);
  }

  @Override
  public String toString() {
    return observer + ": " + observable;
  }

  public Observer<? super M> getObserver() {
    return observer;
  }

  @Override
  public boolean isDisposed() {
    return disposed;
  }

  @Override
  public void dispose() {
    if (!disposed) {
      disposed = true;
      observable.stopObservation(this);
    }
  }
}
