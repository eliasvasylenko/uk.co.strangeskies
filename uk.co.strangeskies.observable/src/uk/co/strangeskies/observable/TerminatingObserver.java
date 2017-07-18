package uk.co.strangeskies.observable;

public class TerminatingObserver<T> extends PassthroughObserver<T, DisposableMessage<T>> {
  public TerminatingObserver(Observer<? super DisposableMessage<T>> downstreamObserver) {
    super(downstreamObserver);
  }

  @Override
  public void onNext(T message) {
    new DisposableMessage<T>() {
      @Override
      public boolean isDisposed() {
        return getObservation().isDisposed();
      }

      @Override
      public void dispose() {
        getObservation().dispose();
      }

      @Override
      public T message() {
        return message;
      }
    };
  }
}
