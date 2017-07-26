package uk.co.strangeskies.observable;

public class UnexpectedMessageException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UnexpectedMessageException(Object message) {
    super("Unexpected message without request " + message);
  }
}
