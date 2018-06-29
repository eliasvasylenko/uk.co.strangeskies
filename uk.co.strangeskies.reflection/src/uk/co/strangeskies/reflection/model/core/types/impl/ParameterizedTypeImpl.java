package uk.co.strangeskies.reflection.model.core.types.impl;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

import uk.co.strangeskies.reflection.ThreadLocalRecursionDetector;

/**
 * Implementing class for ParameterizedType interface. Derived from
 * sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
 */

public class ParameterizedTypeImpl implements ParameterizedType {
  private Type[] actualTypeArguments;
  private Class<?> rawType;
  private Type ownerType;

  public ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
    this.actualTypeArguments = actualTypeArguments;
    this.rawType = rawType;
    if (ownerType != null) {
      this.ownerType = ownerType;
    } else {
      this.ownerType = rawType.getDeclaringClass();
    }
    validateConstructorArguments();
  }

  private void validateConstructorArguments() {
    java.lang.reflect.TypeVariable<?>[] formals = rawType.getTypeParameters();
    // check correct arity of actual type args
    if (formals.length != actualTypeArguments.length) {
      throw new MalformedParameterizedTypeException();
    }
  }

  /**
   * Returns an array of {@code Type} objects representing the actual type
   * arguments to this type.
   *
   * <p>
   * Note that in some cases, the returned array be empty. This can occur if this
   * type represents a non-parameterized type nested within a parameterized type.
   *
   * @return an array of {@code Type} objects representing the actual type
   *         arguments to this type
   * @throws TypeNotPresentException
   *           if any of the actual type arguments refers to a non-existent type
   *           declaration
   * @throws MalformedParameterizedTypeException
   *           if any of the actual type parameters refer to a parameterized type
   *           that cannot be instantiated for any reason
   * @since 1.5
   */
  @Override
  public Type[] getActualTypeArguments() {
    return actualTypeArguments.clone();
  }

  /**
   * Returns the {@code Type} object representing the class or interface that
   * declared this type.
   *
   * @return the {@code Type} object representing the class or interface that
   *         declared this type
   */
  @Override
  public Class<?> getRawType() {
    return rawType;
  }

  /**
   * Returns a {@code Type} object representing the type that this type is a
   * member of. For example, if this type is {@code O<T>.I<S>}, return a
   * representation of {@code O<T>}.
   *
   * <p>
   * If this type is a top-level type, {@code null} is returned.
   *
   * @return a {@code Type} object representing the type that this type is a
   *         member of. If this type is a top-level type, {@code null} is returned
   */
  @Override
  public Type getOwnerType() {
    return ownerType;
  }

  /*
   * From the JavaDoc for java.lang.reflect.ParameterizedType "Instances of
   * classes that implement this interface must implement an equals() method that
   * equates any two instances that share the same generic type declaration and
   * have equal type parameters."
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof ParameterizedType) {
      // Check that information is equivalent
      ParameterizedType that = (ParameterizedType) o;

      if (this == that)
        return true;

      Type thatOwner = that.getOwnerType();
      Type thatRawType = that.getRawType();

      return Objects.equals(ownerType, thatOwner) && Objects.equals(rawType, thatRawType)
          && Arrays
              .equals(
                  actualTypeArguments, // avoid clone
                  that.getActualTypeArguments());
    } else
      return false;
  }

  @Override
  public int hashCode() {
    if (ThreadLocalRecursionDetector.repeatCount(this) == 1) {
      return 0;
    }

    ThreadLocalRecursionDetector.push(this);
    try {
      return Arrays.hashCode(actualTypeArguments) ^ Objects.hashCode(ownerType)
          ^ Objects.hashCode(rawType);
    } finally {
      ThreadLocalRecursionDetector.pop();
    }
  }

  @Override
  public String toString() {
    if (ThreadLocalRecursionDetector.repeatCount(this) == 2) {
      return "...";
    }

    ThreadLocalRecursionDetector.push(this);
    try {
      Type ownerType = getOwnerType();
      Class<?> rawType = getRawType();
      Type[] typeArguments = getActualTypeArguments();

      /*
       * Calculate the string properly, now we're guarded against recursion:
       */
      StringBuilder builder = new StringBuilder();
      if (ownerType == null) {
        builder.append(rawType.getName());
      } else {
        builder.append(ownerType.getTypeName()).append(".");

        if (ownerType instanceof ParameterizedType) {
          String rawTypeName = rawType.getTypeName();
          int index = rawTypeName.indexOf('$');
          if (index > 0) {
            rawTypeName = rawTypeName.substring(index + 1);
          }
          builder.append(rawTypeName);
        } else {
          builder.append(rawType.getTypeName());
        }
      }

      builder.append('<');

      builder.append(stream(typeArguments).map(Type::getTypeName).collect(joining(", ")));

      return builder.append('>').toString();
    } finally {
      ThreadLocalRecursionDetector.pop();
    }
  }
}