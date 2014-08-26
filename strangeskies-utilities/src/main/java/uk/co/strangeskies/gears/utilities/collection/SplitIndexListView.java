package uk.co.strangeskies.gears.utilities.collection;

import java.util.AbstractList;
import java.util.List;

public class SplitIndexListView<T> extends AbstractList<List<T>> {
  private List<? extends T> backingList;
  private int majorSize;

  public SplitIndexListView(List<? extends T> backingList, int majorSize) {
    this.backingList = backingList;
    this.majorSize = majorSize;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final List<T> get(int majorIndex) {
    return (List<T>) backingList.subList(majorIndex, majorSize);
  }

  public final T get(int majorIndex, int minorIndex) {
    return get(majorIndex).get(minorIndex);
  }

  @Override
  public final int size() {
    return backingList.size() / majorSize;
  }
}
