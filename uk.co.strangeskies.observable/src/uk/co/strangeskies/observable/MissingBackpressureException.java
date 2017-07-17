package uk.co.strangeskies.observable;

public class MissingBackpressureException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MissingBackpressureException(Observation<?> observation) {
    super("Source observable does not implement backpressure " + observation);
  }
}
