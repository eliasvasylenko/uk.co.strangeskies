package uk.co.strangeskies.reflection.model.core;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import uk.co.strangeskies.reflection.model.ExtendedTypes;

public interface RuntimeTypes extends ExtendedTypes {
  TypeMirror asMirror(Type type);

  TypeMirror asMirror(Class<?> clazz);

  ArrayType asMirror(GenericArrayType genericArrayType);

  DeclaredType asMirror(ParameterizedType parameterizedType);

  TypeVariable asMirror(java.lang.reflect.TypeVariable<?> typeVariable);

  WildcardType asMirror(java.lang.reflect.WildcardType wildcardType);
}
