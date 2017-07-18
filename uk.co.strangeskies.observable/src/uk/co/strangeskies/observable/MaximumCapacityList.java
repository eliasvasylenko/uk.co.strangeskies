package uk.co.strangeskies.observable;

import java.util.AbstractList;
import java.util.ArrayList;

class MaximumCapacityList<T> extends AbstractList<T> {
  private final ArrayList<T> component = new ArrayList<>();
  private final long capacity;

  public MaximumCapacityList(long capacity) {
    this.capacity = capacity;
  }

  @Override
  public T get(int index) {
    return component.get(index);
  }

  @Override
  public int size() {
    return component.size();
  }

  @Override
  public void add(int index, T element) {
    if (size() + 1 > capacity)
      throw new IndexOutOfBoundsException();

    component.add(index, element);
  }

  @Override
  public T remove(int index) {
    return component.remove(index);
  }
}
