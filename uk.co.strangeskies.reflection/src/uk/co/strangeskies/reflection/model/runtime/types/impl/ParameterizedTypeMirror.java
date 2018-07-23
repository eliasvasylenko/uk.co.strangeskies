package uk.co.strangeskies.reflection.model.runtime.types.impl;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.type.TypeKind.DECLARED;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import uk.co.strangeskies.reflection.ThreadLocalRecursionDetector;
import uk.co.strangeskies.reflection.TypeSubstitution;
import uk.co.strangeskies.reflection.model.runtime.RuntimeModel;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.types.ReifiableRuntimeType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeDeclaredType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeMirror;

public class ParameterizedTypeMirror extends ReifiableRuntimeTypeImpl
    implements RuntimeDeclaredType {
  private final AnnotatedParameterizedType source;
  private List<ReifiableRuntimeType> typeArguments;

  public ParameterizedTypeMirror(RuntimeModel model, AnnotatedParameterizedType source) {
    super(model);
    this.source = source;
  }

  @Override
  public TypeKind getKind() {
    return DECLARED;
  }

  @Override
  public AnnotatedParameterizedType getSource() {
    return source;
  }

  @Override
  public RuntimeElement asElement() {
    return getModel()
        .elements()
        .asMirror((Class<?>) ((ParameterizedType) source.getType()).getRawType());
  }

  @Override
  public RuntimeTypeMirror getEnclosingType() {
    AnnotatedType owner = getSource().getAnnotatedOwnerType();
    if (owner == null) {
      return getModel().types().getNoType(TypeKind.NONE);
    }
    return getModel().types().asMirror(owner);
  }

  @Override
  public List<ReifiableRuntimeType> getTypeArguments() {
    if (typeArguments == null) {
      typeArguments = Stream
          .of(getSource().getAnnotatedActualTypeArguments())
          .map(getModel().types()::asMirror)
          .collect(toList());
    }
    return typeArguments;
  }

  @Override
  public List<RuntimeTypeMirror> directSuperTypes() {
    if (getSource().getType() == java.lang.Object.class) {
      return Collections.emptyList();
    }

    getEnclosingType();

    new TypeSubstitution(getModel().types(), typeArguments);

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
    Type[] actualArgs = getSource().getAnnotatedActualTypeArguments();

    return ParameterizedTypeImpl.make(raw, Arrays.copyOf(actualArgs, actualArgs.length), null);
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

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitDeclared(this, p);
  }
}