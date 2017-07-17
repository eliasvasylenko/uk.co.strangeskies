package uk.co.strangeskies.observable;

public interface OwnedMessage<O, M> {
  O owner();

  M message();
}
