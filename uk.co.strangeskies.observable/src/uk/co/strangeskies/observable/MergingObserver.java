package uk.co.strangeskies.observable;

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MergingObserver<T, U> extends PassthroughObserver<T, U> {
  private final Function<? super T, ? extends Observable<? extends U>> mapping;
  private final List<Observation> observations;
  private boolean cancelled;

  public MergingObserver(
      Observer<? super U> downstreamObserver,
      Function<? super T, ? extends Observable<? extends U>> mapping) {
    super(downstreamObserver);
    this.mapping = mapping;
    this.observations = new ArrayList<>();
  }

  @Override
  public void onObserve(Observation observation) {
    super.onObserve(new Observation() {
      @Override
      public void cancel() {
        observation.cancel();
        cancelled = true;
        synchronized (observations) {
          observations.forEach(Observation::cancel);
        }
      }

      @Override
      public void request(long count) {
        synchronized (observations) {
          if (count == Long.MAX_VALUE) {
            observations.forEach(Observation::requestUnbounded);
          } else {
            observations.sort(comparing(Observation::getPendingRequestCount));
            // TODO balance request between upstream observations
          }
        }
      }

      @Override
      public long getPendingRequestCount() {
        synchronized (observations) {
          return observations.stream().mapToLong(Observation::getPendingRequestCount).sum();
        }
      }
    });
    observation.requestUnbounded();
  }

  @Override
  public void onNext(T message) {
    synchronized (observations) {
      if (!cancelled) {
        mapping
            .apply(message)
            .then(Observer.onObservation(observations::add))
            .then(Observer.onFailure(this::onFail))
            .then(Observer.onCompletion(this::onComplete))
            .observe(getDownstreamObserver()::onNext);
      }
    }
  }
}