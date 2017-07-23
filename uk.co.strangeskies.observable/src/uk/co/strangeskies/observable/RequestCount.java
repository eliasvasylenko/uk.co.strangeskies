package uk.co.strangeskies.observable;

public class RequestCount {
  private long requestCount;

  public synchronized void request(long count) {
    if (requestCount >= 0)
      requestCount += count;
  }

  public synchronized void requestUnbounded() {
    requestCount = -1;
  }

  public synchronized boolean isFulfilled() {
    return requestCount != 0;
  }

  public synchronized void fulfil() {
    if (requestCount > 0)
      requestCount--;
    else if (requestCount == 0)
      throw new IllegalStateException("No request to fulfil");
  }
}
