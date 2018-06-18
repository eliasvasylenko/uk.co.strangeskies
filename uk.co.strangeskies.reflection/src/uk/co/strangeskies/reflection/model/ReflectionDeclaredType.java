package uk.co.strangeskies.reflection.model;

import static java.util.Arrays.stream;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.ThreadLocalRecursionDetector;

abstract class ReflectionDeclaredType extends ReflectionTypeMirror
    implements javax.lang.model.type.DeclaredType {
  private Class<?> source = null;

  private ReflectionDeclaredType(Class<?> source) {
    super(TypeKind.DECLARED);
    this.source = source;
  }

  static DeclaredType instance(Class<?> source, Type genericSource) {
    if (genericSource instanceof ParameterizedType) {
      return new ParameterizedDeclaredType(source, (ParameterizedType) genericSource);
    } else if (genericSource instanceof Class) { // This happens when a field has a raw type
      if (!source.equals(genericSource)) {
        throw new IllegalArgumentException("Don't know how to handle this");
      }
      return instance(source);
    }
    throw new IllegalArgumentException(
        "Don't know how to create a declared type from: " + source + " and genericSource "
            + genericSource);
  }

  static DeclaredType instance(Class<?> source) {
    return new RawDeclaredType(source);
  }

  protected Class<?> getSource() {
    return source;
  }

  @Override
  public Element asElement() {
    return CoreReflectionFactory.createMirror(getSource());
  }

  abstract boolean isSameType(DeclaredType other);

  @Override
  TypeMirror capture() {
    return TypeVariableCapture.captureWildcardArguments(this);
  }

  private static class RawDeclaredType extends ReflectionDeclaredType
      implements ReifiableReflectionType {
    private RawDeclaredType(Class<?> source) {
      super(source);
    }

    @Override
    public Class<?> getSource() {
      return super.getSource();
    }

    @Override
    public TypeMirror getEnclosingType() {
      Class<?> enclosing = getSource().getEnclosingClass();
      if (null == enclosing) {
        return ReflectionNoType.getNoneInstance();
      } else {
        return TypeFactory.instance(enclosing);
      }
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments() {
      return Collections.emptyList();
    }

    @Override
    List<? extends TypeMirror> directSuperTypes() {
      if (getSource().isEnum()) {
        return enumSuper();
      }

      if (getSource() == java.lang.Object.class) {
        return Collections.emptyList();
      }
      List<TypeMirror> res = new ArrayList<>();
      Type[] superInterfaces = getSource().getInterfaces();
      if (!getSource().isInterface()) {
        res.add(TypeFactory.instance(getSource().getSuperclass()));
      } else if (superInterfaces.length == 0) {
        // Interfaces that don't extend another interface
        // have java.lang.Object as a direct supertype.
        return Collections
            .unmodifiableList(Arrays.asList(TypeFactory.instance(java.lang.Object.class)));
      }

      for (Type t : superInterfaces) {
        res.add(TypeFactory.instance(t));
      }
      return Collections.unmodifiableList(res);
    }

    private List<? extends TypeMirror> enumSuper() {
      Class<?> rawSuper = getSource().getSuperclass();
      Type[] actualArgs = ((ReflectionDeclaredType.ParameterizedTypeImpl) getSource()
          .getGenericSuperclass()).getActualTypeArguments();

      // Reconsider this : assume the problem is making
      // Enum<MyEnum> rather than just a raw enum.
      return Collections
          .unmodifiableList(
              Arrays
                  .asList(
                      TypeFactory
                          .instance(
                              ParameterizedTypeImpl
                                  .make(
                                      rawSuper,
                                      Arrays.copyOf(actualArgs, actualArgs.length),
                                      null))));
    }

    @Override
    boolean isSameType(DeclaredType other) {
      if (other instanceof ReflectionDeclaredType.RawDeclaredType) {
        return Objects
            .equals(getSource(), ((ReflectionDeclaredType.RawDeclaredType) other).getSource());
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      return getSource().toString();
    }
  }

  private static class ParameterizedDeclaredType extends ReflectionDeclaredType {
    private ParameterizedType genericSource = null;

    private ParameterizedDeclaredType(Class<?> source, ParameterizedType genericSource) {
      super(source);
      this.genericSource = genericSource;
    }

    @Override
    public TypeMirror getEnclosingType() {
      Type me = genericSource;
      Type owner = GenericTypes.getEnclosingType(me);
      if (owner == null) {
        return ReflectionNoType.getNoneInstance();
      }
      return TypeFactory.instance(owner);
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments() {
      Type[] typeArgs = genericSource.getActualTypeArguments();

      int length = typeArgs.length;
      if (length == 0)
        return Collections.emptyList();
      else {
        List<TypeMirror> tmp = new ArrayList<>(length);
        for (Type t : typeArgs) {
          tmp.add(TypeFactory.instance(t));
        }
        return Collections.unmodifiableList(tmp);
      }
    }

    @Override
    List<? extends TypeMirror> directSuperTypes() {
      if (getSource() == java.lang.Object.class) {
        return Collections.emptyList();
      }

      List<TypeMirror> res = new ArrayList<>();
      Type[] superInterfaces = getSource().getGenericInterfaces();
      if (!getSource().isInterface()) {
        // Replace actual type arguments with our type arguments
        res.add(TypeFactory.instance(substituteTypeArgs(getSource().getGenericSuperclass())));
      } else if (superInterfaces.length == 0) {
        // Interfaces that don't extend another interface
        // have java.lang.Object as a direct supertype, plus
        // possibly the interface's raw type
        res.add(TypeFactory.instance(java.lang.Object.class));
      }

      for (Type t : superInterfaces) {
        res.add(TypeFactory.instance(substituteTypeArgs(t)));
      }

      res.add(TypeFactory.instance(getSource())); // Add raw type
      return Collections.unmodifiableList(res);
    }

    private Type substituteTypeArgs(Type type) {
      if (!(type instanceof ParameterizedType)) {
        return type;
      }

      ParameterizedType target = (ParameterizedType) type;
      // Cast to get a Class instead of a plain type.
      Class<?> raw = ((ReflectionDeclaredType.ParameterizedTypeImpl) target).getRawType();
      Type[] actualArgs = genericSource.getActualTypeArguments();

      return ParameterizedTypeImpl.make(raw, Arrays.copyOf(actualArgs, actualArgs.length), null);
    }

    @Override
    boolean isSameType(DeclaredType other) {
      if (other instanceof ReflectionDeclaredType.ParameterizedDeclaredType) {
        return GenericTypes
            .isSameGenericType(
                genericSource,
                ((ReflectionDeclaredType.ParameterizedDeclaredType) other).genericSource);
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      return getKind().toString() + " " + genericSource.toString();
    }
  }

  /**
   * Implementing class for ParameterizedType interface. Derived from
   * sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
   */

  private static class ParameterizedTypeImpl implements ParameterizedType {
    private Type[] actualTypeArguments;
    private Class<?> rawType;
    private Type ownerType;

    private ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
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
      java.lang.reflect.TypeVariable/* <?> */[] formals = rawType.getTypeParameters();
      // check correct arity of actual type args
      if (formals.length != actualTypeArguments.length) {
        throw new MalformedParameterizedTypeException();
      }
    }

    /**
     * Static factory. Given a (generic) class, actual type arguments and an owner
     * type, creates a parameterized type. This class can be instantiated with a a
     * raw type that does not represent a generic type, provided the list of actual
     * type arguments is empty. If the ownerType argument is null, the declaring
     * class of the raw type is used as the owner type.
     * <p>
     * This method throws a MalformedParameterizedTypeException under the following
     * circumstances: If the number of actual type arguments (i.e., the size of the
     * array {@code typeArgs}) does not correspond to the number of formal type
     * arguments. If any of the actual type arguments is not an instance of the
     * bounds on the corresponding formal.
     * 
     * @param rawType
     *          the Class representing the generic type declaration being
     *          instantiated
     * @param actualTypeArguments
     *          - a (possibly empty) array of types representing the actual type
     *          arguments to the parameterized type
     * @param ownerType
     *          - the enclosing type, if known.
     * @return An instance of {@code ParameterizedType}
     * @throws MalformedParameterizedTypeException
     *           - if the instantiation is invalid
     */
    public static ReflectionDeclaredType.ParameterizedTypeImpl make(
        Class<?> rawType,
        Type[] actualTypeArguments,
        Type ownerType) {
      return new ParameterizedTypeImpl(rawType, actualTypeArguments, ownerType);
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
          builder.append(typeParser.compose(ownerType)).append(".");

          if (ownerType instanceof ParameterizedType) {
            String rawTypeName = rawType.getTypeName();
            int index = rawTypeName.indexOf('$');
            if (index > 0) {
              rawTypeName = rawTypeName.substring(index + 1);
            }
            builder.append(rawTypeName);
          } else {
            builder.append(imports.getClassName(rawType));
          }
        }

        builder.append('<');

        builder
            .append(stream(typeArguments).map(t -> typeParser.compose(t)).collect(joining(", ")));

        return builder.append('>').toString();
      } finally {
        ThreadLocalRecursionDetector.pop();
      }
    }
  }

}