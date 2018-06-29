package uk.co.strangeskies.reflection.model.core.types.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class ParameterizedDeclaredType extends ReflectionDeclaredType {
  private ParameterizedType genericSource = null;

  public ParameterizedDeclaredType(ParameterizedType genericSource) {
    super((Class<?>) genericSource.getRawType());
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
  public List<? extends TypeMirror> directSuperTypes() {
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
    Class<?> raw = ((ParameterizedTypeImpl) target).getRawType();
    Type[] actualArgs = genericSource.getActualTypeArguments();

    return ParameterizedTypeImpl.make(raw, Arrays.copyOf(actualArgs, actualArgs.length), null);
  }

  @Override
  boolean isSameType(DeclaredType other) {
    if (other instanceof ParameterizedDeclaredType) {
      return GenericTypes
          .isSameGenericType(genericSource, ((ParameterizedDeclaredType) other).genericSource);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return getKind().toString() + " " + genericSource.toString();
  }
}