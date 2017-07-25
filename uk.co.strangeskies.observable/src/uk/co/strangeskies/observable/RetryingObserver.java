package uk.co.strangeskies.observable;

public class RetryingObserver<T> extends PassthroughObserver<T, T> {
  private final Observable<? extends T> retryOn;

  public RetryingObserver(Observer<? super T> downstreamObserver, Observable<? extends T> retryOn) {
    super(downstreamObserver);

    this.retryOn = retryOn;
  }

  @Override
  public void onFail(Throwable t) {
    retryOn.observe(this);
  }

  @Override
  public void onNext(T message) {
    getDownstreamObserver().onNext(message);
  }
}
