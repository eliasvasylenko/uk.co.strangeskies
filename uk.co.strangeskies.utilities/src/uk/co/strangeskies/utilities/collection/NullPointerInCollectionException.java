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

import java.util.Collection;

public class NullPointerInCollectionException extends NullPointerException {
  private static final long serialVersionUID = 1L;

  public NullPointerInCollectionException(int index, Collection<?> list) {
    super("Null pointer at index '" + index + "' of '" + list + "'");
  }

  public NullPointerInCollectionException(int index, Object... list) {
    super("Null pointer at index '" + index + "' of '" + list + "'");
  }

  public static void checkList(Collection<?> list)
      throws NullPointerInCollectionException {
    int index = 0;
    for (Object element : list) {
      if (element == null) {
        throw new NullPointerInCollectionException(index, list);
      }
      index++;
    }
  }

  public static void checkList(Object[] list)
      throws NullPointerInCollectionException {
    for (int index = 0; index < list.length; index++) {
      if (list[index] == null) {
        throw new NullPointerInCollectionException(index, list);
      }
    }
  }
}
