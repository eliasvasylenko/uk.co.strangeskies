package uk.co.strangeskies.observable;

import java.util.ArrayList;
import java.util.List;

public class MergingObservable<M> implements Observable<M> {
  private final Observable<? extends Observable<? extends M>> parentObservables;

  public MergingObservable(Observable<? extends Observable<? extends M>> parentObservables) {
    this.parentObservables = parentObservables;
  }

  @Override
  public Observation<M> observe(Observer<? super M> observer) {
    List<Observable<? extends M>> parentObservables = new ArrayList<>();

    Observation<? extends Observable<? extends M>> parentObservablesObservation = this.parentObservables
        .observe(parentObservables::add);
    parentObservablesObservation.requestUnbounded();

    return new Observation<M>() {
      @Override
      public boolean isDisposed() {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public void dispose() {
        parentObservablesObservation.dispose();
      }

      @Override
      public void request(long count) {
        // TODO Auto-generated method stub
        Observation.super.request(count);
      }

      @Override
      public void requestUnbounded() {
        // TODO Auto-generated method stub
        Observation.super.requestUnbounded();
      }
    };
  }
}
