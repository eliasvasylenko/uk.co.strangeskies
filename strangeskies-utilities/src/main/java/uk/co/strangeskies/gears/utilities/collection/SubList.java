package uk.co.strangeskies.gears.utilities.collection;

import java.util.AbstractList;
import java.util.List;

public class SubList<T> extends AbstractList<T> {
  private final List<T> backingList;

  private int startPosition = 0;
  private int endPosition = 0;

  public SubList(List<T> backingList, int startPosition, int endPosition) {
    this.backingList = backingList;

    this.startPosition = startPosition;
    this.endPosition = endPosition;
  }

  @Override
  public int size() {
    return endPosition - startPosition;
  }

  @Override
  public T get(int index) {
    if (index + startPosition >= endPosition) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    return backingList.get(index + startPosition);
  }
}
