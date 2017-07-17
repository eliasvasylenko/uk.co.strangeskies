package uk.co.strangeskies.observable;

/**
 * This is a helper class for implementing {@link Observable passive
 * observables}.
 * <p>
 * A passive observable is one which does not maintain a set of observations or
 * manage its own events, instead deferring to one or more parents. When an
 * observer subscribes to a passive observable, typically the observer is
 * decorated, and the decorator is then subscribed to the parents. This way the
 * decorator can modify, inspect, or filter events as appropriate before passing
 * them back through to the original observer.
 * <p>
 * This class is a partial implementation of such a decorator, taking care of
 * the subscription process and providing default event handling implementations
 * which simply pass the events along without modification.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The message type of the parent observables
 * @param <M>
 *          The message type of the passive observable
 */
public abstract class PassthroughObservation<T, M> implements Observer<T>, Observation<M> {
  private Observation<? extends T> parentObservation;

  protected Observation<M> passthroughObservation(Observable<? extends T> parentObservable) {
    parentObservable.observe(new Observer<T>() {
      @Override
      public void onObserve(Observation<? extends T> observation) {
        if (parentObservation != null)
          throw new IllegalStateException(
              "Cannot use observation more than once " + PassthroughObservation.this);
        parentObservation = observation;
        PassthroughObservation.this.onObserve(parentObservation);
      }

      @Override
      public void onNext(T message) {
        PassthroughObservation.this.onNext(message);
      }

      @Override
      public void onComplete() {
        PassthroughObservation.this.onComplete();
      }

      @Override
      public void onFail(Throwable t) {
        PassthroughObservation.this.onFail(t);
      }
    });
    return this;
  }

  @Override
  public void onObserve(Observation<? extends T> subscription) {
    onObserve();
  }

  @Override
  public abstract void onComplete();

  @Override
  public abstract void onFail(Throwable t);

  public abstract void onObserve();

  public Observation<? extends T> getParentObservation() {
    return parentObservation;
  }

  @Override
  public String toString() {
    return getParentObservation() + " -> " + getClass().getSimpleName();
  }

  @Override
  public boolean isDisposed() {
    return getParentObservation().isDisposed();
  }

  @Override
  public void dispose() {
    getParentObservation().dispose();
  }

  @Override
  public void request(long count) {
    getParentObservation().request(count);
  }

  @Override
  public void requestUnbounded() {
    getParentObservation().requestUnbounded();
  }
}
