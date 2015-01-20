/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection;

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
