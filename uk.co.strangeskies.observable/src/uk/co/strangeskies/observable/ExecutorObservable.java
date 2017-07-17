package uk.co.strangeskies.observable;

import java.util.concurrent.Executor;

public class ExecutorObservable<M> implements Observable<M> {
  public ExecutorObservable(Observable<M> observable, Executor executor) {
    // TODO Auto-generated constructor stub
  }

  @Override
  public Observation<M> observe(Observer<? super M> observer) {
    // TODO Auto-generated method stub
    return null;
  }
}
