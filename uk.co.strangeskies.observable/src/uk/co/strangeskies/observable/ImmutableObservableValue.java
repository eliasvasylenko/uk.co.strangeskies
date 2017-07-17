package uk.co.strangeskies.observable;

public interface ImmutableObservableValue<T> extends ImmutableObservable<T>, ObservableValue<T> {
  @Override
  default Observable<Change<T>> changes() {
    return ImmutableObservable.instance();
  }
}