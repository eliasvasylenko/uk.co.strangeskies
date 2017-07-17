package uk.co.strangeskies.observable;

public interface ImmutableObservable<T> extends Observable<T> {
  @SuppressWarnings("unchecked")
  static <T> Observable<T> instance() {
    return (Observable<T>) INSTANCE;
  }

  static Observable<?> INSTANCE = new ImmutableObservable<Object>() {};

  @Override
  default Observation<T> observe(Observer<? super T> observer) {
    Observation<T> observation = new Observation<T>() {
      private boolean disposed = false;

      @Override
      public boolean isDisposed() {
        return disposed;
      }

      @Override
      public void dispose() {
        disposed = true;
      }
    };
    observer.onObserve(observation);
    return observation;
  }
}