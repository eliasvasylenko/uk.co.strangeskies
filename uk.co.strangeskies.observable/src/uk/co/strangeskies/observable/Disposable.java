package uk.co.strangeskies.observable;

public interface Disposable {
  boolean isDisposed();

  void dispose();
}
