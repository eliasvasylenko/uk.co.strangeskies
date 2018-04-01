package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class PrimitiveTypes {
  private static final Map<Class<?>, Class<?>> WRAPPED_PRIMITIVES = Collections
      .unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
        private static final long serialVersionUID = 1L;

        {
          put(void.class, Void.class);
          put(boolean.class, Boolean.class);
          put(byte.class, Byte.class);
          put(char.class, Character.class);
          put(short.class, Short.class);
          put(int.class, Integer.class);
          put(long.class, Long.class);
          put(float.class, Float.class);
          put(double.class, Double.class);
        }
      });

  private static final Map<Class<?>, Class<?>> UNWRAPPED_PRIMITIVES = Collections
      .unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
        private static final long serialVersionUID = 1L;

        {
          for (Class<?> primitive : WRAPPED_PRIMITIVES.keySet())
            put(WRAPPED_PRIMITIVES.get(primitive), primitive);
        }
      });

  /**
   * Get all primitive type classes
   * 
   * @return A set containing all primitive types.
   */
  public static Stream<Class<?>> getPrimitives() {
    return WRAPPED_PRIMITIVES.keySet().stream();
  }

  /**
   * Is the given type a primitive type as per the Java type system.
   * 
   * @param type
   *          The type we wish to classify.
   * @return True if the type is primitive, false otherwise.
   */
  public static boolean isPrimitive(Type type) {
    return WRAPPED_PRIMITIVES.keySet().contains(type);
  }

  /**
   * Is the type a wrapper for a primitive type as per the Java type system.
   * 
   * @param type
   *          The type we wish to classify.
   * @return True if the type is a primitive wrapper, false otherwise.
   */
  public static boolean isPrimitiveWrapper(Type type) {
    return UNWRAPPED_PRIMITIVES.keySet().contains(type);
  }

  /**
   * If this TypeToken is a primitive type, determine the wrapped primitive type.
   * 
   * @param <T>
   *          The type we wish to wrap.
   * @param type
   *          The type we wish to wrap.
   * @return The wrapper type of the primitive type this TypeToken represents,
   *         otherwise this TypeToken itself.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Type> T wrapPrimitive(T type) {
    if (isPrimitive(type))
      return (T) WRAPPED_PRIMITIVES.get(type);
    else
      return type;
  }

  /**
   * If this TypeToken is a wrapper of a primitive type, determine the unwrapped
   * primitive type.
   * 
   * @param <T>
   *          The type we wish to unwrap.
   * @param type
   *          The type we wish to unwrap.
   * @return The primitive type wrapped by this TypeToken, otherwise this
   *         TypeToken itself.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Type> T unwrapPrimitive(T type) {
    if (isPrimitiveWrapper(type))
      return (T) UNWRAPPED_PRIMITIVES.get(type);
    else
      return type;
  }
}
