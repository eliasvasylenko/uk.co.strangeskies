package uk.co.strangeskies.gears.utilities.collection;

import java.util.AbstractList;
import java.util.List;

public class MergeIndicesListView<T> extends AbstractList<T> {
  private final List<? extends List<? extends T>> backingList;

  public MergeIndicesListView(List<? extends List<? extends T>> backingList) {
    this.backingList = backingList;
  }

  @Override
  public final T get(int index) {
    int size = 0;
    int previousSize = 0;

    for (List<? extends T> major : backingList) {
      size += major.size();

      if (size > index) {
        return major.get(index - previousSize);
      }

      previousSize = size;
    }

    throw new ArrayIndexOutOfBoundsException();
  }

  @Override
  public final int size() {
    int size = 0;

    for (List<?> major : backingList) {
      size += major.size();
    }

    return size;
  }
}
