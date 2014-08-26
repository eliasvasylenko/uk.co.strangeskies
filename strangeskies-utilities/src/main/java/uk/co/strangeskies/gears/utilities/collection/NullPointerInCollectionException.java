package uk.co.strangeskies.gears.utilities.collection;

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
