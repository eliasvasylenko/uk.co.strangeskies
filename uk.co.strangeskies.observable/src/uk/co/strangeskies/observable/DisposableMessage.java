package uk.co.strangeskies.observable;

public interface DisposableMessage<T> extends Disposable {
  T message();
}
