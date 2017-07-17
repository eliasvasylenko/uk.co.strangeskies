package uk.co.strangeskies.observable;

import java.util.concurrent.Executor;

public class LatestObservation<M> extends PassthroughObservation<M, M> {
  private final Observer<? super M> observer;
  private final Executor executor;

  public LatestObservation(Observer<? super M> observer, Executor executor) {
    this.observer = observer;
    this.executor = executor;
  }

  private Runnable latest;

  @Override
  public void onObserve() {
    observer.onObserve(this);
  }

  @Override
  public void onNext(M message) {
    setLatest(() -> observer.onNext(message));
  }

  @Override
  public void onComplete() {
    setLatest(observer::onComplete);
  }

  @Override
  public void onFail(Throwable t) {
    setLatest(() -> observer.onFail(t));
  }

  public synchronized void setLatest(Runnable item) {
    boolean ready = latest == null;
    latest = item;
    if (ready)
      executor.execute(() -> getLatest().run());
  }

  private synchronized Runnable getLatest() {
    Runnable latest = this.latest;
    this.latest = null;
    return latest;
  }
}
