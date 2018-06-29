/*
 * Copyright (C) 2018 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.model.core.types.impl;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.type.TypeKind.TYPEVAR;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;

import sun.reflect.generics.factory.CoreReflectionFactory;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeSubstitution;
import uk.co.strangeskies.reflection.inference.BoundSet;
import uk.co.strangeskies.reflection.inference.InferenceVariable;
import uk.co.strangeskies.reflection.inference.InferenceVariableBounds;
import uk.co.strangeskies.reflection.model.core.impl.RuntimeTypesImpl;

/**
 * A representation of an unknown instantiation of a type variable or inference
 * variable which is known to satisfy a certain set of upper and lower bonds.
 * 
 * @author Elias N Vasylenko
 */
public class TypeVariableCapture implements TypeVariable {
  private static final TypeMirror DEFAULT_UPPER_BOUND = CoreReflectionFactory
      .createTypeMirror(Object.class);
  private static final TypeMirror DEFAULT_LOWER_BOUND = RuntimeTypesImpl.instance().getNullType();

  private final String name;

  private TypeMirror upperBound;
  private TypeMirror lowerBound;

  private TypeVariableCapture() {
    this.name = "CAP";
  }

  private TypeVariableCapture(String capturedType) {
    this.name = "CAP_" + capturedType;
  }

  private final void complete() {
    if (upperBound != null && lowerBound != null
        && !RuntimeTypesImpl.instance().isAssignable(lowerBound, upperBound)) {
      throw new ReflectionException(REFLECTION_PROPERTIES.invalidTypeVariableCaptureBounds(this));
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(name);

    if (upperBound != null)
      builder.append(" extends ").append(upperBound);

    if (lowerBound != null)
      builder.append(" super ").append(lowerBound);

    return builder.toString();
  }

  /**
   * @return The upper bounds of this TypeVariableCapture.
   */
  @Override
  public TypeMirror getUpperBound() {
    return upperBound == null ? DEFAULT_UPPER_BOUND : upperBound;
  }

  /**
   * @return The lower bounds of this TypeVariableCapture.
   */
  @Override
  public TypeMirror getLowerBound() {
    return lowerBound == null ? DEFAULT_LOWER_BOUND : lowerBound;
  }

  @Override
  public TypeKind getKind() {
    return TYPEVAR;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitTypeVariable(this, p);
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Element asElement() {
    // TODO Auto-generated method stub
    return null;
  }

  public static TypeMirror captureWildcardArguments(TypeMirror type) {
    switch (type.getKind()) {
    case DECLARED:
      return captureWildcardArguments((DeclaredType) type);

    case ARRAY:
      return captureWildcardArguments((ArrayType) type);

    default:
      throw new ReflectionException(REFLECTION_PROPERTIES.cannotCaptureTypeOfKind(type));
    }
  }

  /**
   * Capture new type variable instantiations over any wildcard arguments of the
   * given generic array type.
   * 
   * @param type
   *          The generic array type whose arguments we wish to capture.
   * @return A new parameterized type of the same class as the passed type,
   *         parameterized with the captures of the original arguments.
   */
  public static ArrayType captureWildcardArguments(ArrayType type) {
    return RuntimeTypesImpl
        .instance()
        .getArrayType(captureWildcardArguments(type.getComponentType()));
  }

  /**
   * Capture new type variable instantiations over any wildcard arguments of the
   * given parameterized type.
   * 
   * @param type
   *          The parameterized type whose arguments we wish to capture.
   * @return A new parameterized type of the same class as the passed type,
   *         parameterized with the captures of the original arguments.
   */
  public static DeclaredType captureWildcardArguments(DeclaredType type) {
    return captureWildcardArguments(type, new LinkedHashMap<>());
  }

  private static DeclaredType captureWildcardArguments(
      DeclaredType type,
      Map<TypeVariable, TypeMirror> allArguments) {
    TypeMirror enclosingType = type.getEnclosingType();
    if (enclosingType.getKind() == TypeKind.DECLARED) {
      enclosingType = captureWildcardArguments((DeclaredType) enclosingType, allArguments);
    }

    var element = (TypeElement) type.asElement();
    var parameters = element
        .getTypeParameters()
        .stream()
        .map(TypeParameterElement::asType)
        .map(TypeVariable.class::cast)
        .collect(toList());
    var arguments = type.getTypeArguments();

    Map<TypeVariableCapture, WildcardType> captures = new HashMap<>();

    for (int i = 0; i < parameters.size(); i++) {
      var parameter = parameters.get(i);
      var argument = arguments.get(i);

      if (argument.getKind() == TypeKind.WILDCARD) {
        var capture = new TypeVariableCapture(parameter.toString());
        capture.upperBound = parameter.getUpperBound();
        capture.lowerBound = parameter.getLowerBound();
        captures.put(capture, (WildcardType) argument);

        allArguments.put(parameter, capture);
      } else {
        allArguments.put(parameter, argument);
      }
    }

    if (captures.isEmpty()) {
      return type;
    }

    var substitution = new TypeSubstitution(RuntimeTypesImpl.instance(), allArguments);
    for (var capture : captures.keySet()) {
      capture.upperBound = substitution.resolve(capture.upperBound);
      capture.lowerBound = substitution.resolve(capture.lowerBound);

      WildcardType wildcard = captures.get(capture);

      if (DEFAULT_UPPER_BOUND.equals(capture.upperBound)) {
        capture.upperBound = wildcard.getExtendsBound();
      } else {
        capture.upperBound = RuntimeTypesImpl
            .instance()
            .getIntersection(capture.upperBound, wildcard.getExtendsBound());
      }
      if (DEFAULT_LOWER_BOUND.equals(capture.lowerBound)) {
        capture.lowerBound = wildcard.getExtendsBound();
      } else {
        capture.lowerBound = RuntimeTypesImpl
            .instance()
            .getIntersection(capture.lowerBound, wildcard.getSuperBound());
      }

      capture.complete();
    }

    var typeArgs = parameters.stream().map(allArguments::get).toArray(TypeMirror[]::new);
    if (enclosingType.getKind() == TypeKind.DECLARED) {
      return RuntimeTypesImpl
          .instance()
          .getDeclaredType((DeclaredType) enclosingType, element, typeArgs);
    } else {
      return RuntimeTypesImpl.instance().getDeclaredType(element, typeArgs);
    }
  }

  /**
   * Capture new type variable instantiation over a given wildcard type.
   * 
   * @param type
   *          The parameterized type whose arguments we wish to capture.
   * @return A new parameterized type of the same class as the passed type,
   *         parameterized with the captures of the original arguments.
   */
  public static TypeVariableCapture captureWildcard(WildcardType type) {
    TypeVariableCapture capture = new TypeVariableCapture();
    capture.upperBound = type.getExtendsBound();
    capture.lowerBound = type.getSuperBound();
    capture.complete();
    return capture;
  }

  /**
   * Capture fresh type variables as valid stand-in instantiations for a set of
   * inference variables.
   * 
   * @param types
   *          The inference variables to capture.
   * @param bounds
   *          The context from which to determine the current bounds on the given
   *          inference variables, and to incorporate new bounds into.
   * @return A mapping from the inference variables passes to their new captures.
   */
  public static BoundSet captureInferenceVariables(
      Collection<? extends InferenceVariable> types,
      BoundSet bounds) {
    TypeSubstitution properTypeSubstitutuion = properTypeSubstitution(types, bounds);

    Map<InferenceVariable, TypeMirror> typeVariableCaptures = new HashMap<>();
    for (InferenceVariable inferenceVariable : types) {
      Optional<TypeMirror> existingMatch = bounds
          .getBoundsOn(inferenceVariable)
          .getEqualities()
          .filter(typeVariableCaptures::containsKey)
          .findAny();

      if (existingMatch.isPresent()) {
        /*
         * We have already captured an inference variable equal with this one
         */
        typeVariableCaptures.put(inferenceVariable, typeVariableCaptures.get(existingMatch.get()));
      } else {
        Set<TypeMirror> equalitySet = new HashSet<>();

        for (TypeMirror equality : bounds
            .getBoundsOn(inferenceVariable)
            .getEqualities()
            .collect(toList())) {
          if (!(equality instanceof InferenceVariable)) {
            try {
              equalitySet.add(properTypeSubstitutuion.resolve(equality));
            } catch (ReflectionException e) {
              equalitySet.clear();
              break;
            }
          }
        }

        if (!equalitySet.isEmpty()) {
          typeVariableCaptures
              .put(inferenceVariable, RuntimeTypesImpl.instance().getIntersection(equalitySet));
        } else {
          /*
           * For all i (1 ≤ i ≤ n), if αi has one or more proper lower bounds L1, ..., Lk,
           * then let the lower bound of Yi be lub(L1, ..., Lk); if not, then Yi has no
           * lower bound.
           */
          TypeMirror[] lowerBoundSet = bounds
              .getBoundsOn(inferenceVariable)
              .getLowerBounds()
              .filter(InferenceVariable::isProperType)
              .toArray(TypeMirror[]::new);

          TypeMirror lowerBound;
          if (lowerBoundSet.length == 0)
            lowerBound = RuntimeTypesImpl.instance().getNullType();
          else {
            lowerBound = RuntimeTypesImpl.instance().getIntersection(lowerBoundSet);
          }

          /*
           * For all i (1 ≤ i ≤ n), where αi has upper bounds U1, ..., Uk, let the upper
           * bound of Yi be glb(U1 θ, ..., Uk θ), where θ is the substitution [α1:=Y1,
           * ..., αn:=Yn].
           */

          BoundSet finalBounds = bounds;
          Set<TypeMirror> upperBoundSet = bounds
              .getBoundsOn(inferenceVariable)
              .getUpperBounds()
              .map(t -> {
                try {
                  return properTypeSubstitutuion.resolve(t);
                } catch (ReflectionException e) {
                  throw new ReflectionException(
                      REFLECTION_PROPERTIES.improperUpperBound(t, inferenceVariable, finalBounds),
                      e);
                }
              })
              .collect(Collectors.toSet());

          /*
           * no need to be checked properly here, as we do this later in #substituteBounds
           */
          TypeMirror upperBound = upperBoundSet.toArray(new TypeMirror[upperBoundSet.size()]);

          /*
           * If the type variables Y1, ..., Yn do not have well-formed bounds (that is, a
           * lower bound is not a subtype of an upper bound, or an intersection type is
           * inconsistent), then resolution fails.
           */
          TypeVariableCapture capture = new TypeVariableCapture(inferenceVariable.toString());
          capture.upperBound = upperBound;
          capture.lowerBound = lowerBound;

          typeVariableCaptures.put(inferenceVariable, capture);
        }
      }
    }

    substituteInferenceVariableBounds(typeVariableCaptures);

    return bounds.withInstantiations(typeVariableCaptures);
  }

  static void substituteInferenceVariableBounds(Map<InferenceVariable, TypeMirror> captures) {
    TypeSubstitution substitution = new TypeSubstitution(RuntimeTypesImpl.instance(), captures);

    for (InferenceVariable type : captures.keySet()) {
      if (captures.get(type) instanceof TypeVariableCapture) {
        TypeVariableCapture capture = (TypeVariableCapture) captures.get(type);

        capture.upperBound = substitution.resolve(capture.upperBound);
        capture.lowerBound = substitution.resolve(capture.lowerBound);
      } else {
        TypeMirror capture = substitution.resolve(captures.get(type));
        if (capture instanceof IntersectionType) {
          capture = RuntimeTypesImpl.instance().getIntersection(capture);
        }
        captures.put(type, capture);
      }
    }

    for (TypeMirror type : captures.values()) {
      if (type instanceof TypeVariableCapture) {
        TypeVariableCapture capture = (TypeVariableCapture) type;

        capture.upperBound = RuntimeTypesImpl.instance().getIntersection(capture.upperBound);

        if (!InferenceVariable.isProperType(capture)) {
          throw new ReflectionException(REFLECTION_PROPERTIES.improperCaptureType(capture));
        }
      }
    }
  }

  private static TypeSubstitution properTypeSubstitution(
      Collection<? extends InferenceVariable> types,
      BoundSet bounds) {
    return new TypeSubstitution(RuntimeTypesImpl.instance())
        .where(InferenceVariable.class::isInstance, i -> {
          InferenceVariableBounds inferenceVariableBounds = bounds
              .getBoundsOn((InferenceVariable) i);

          /*
           * The intent of this substitution is to replace all instances of inference
           * variables with proper forms where possible.
           * 
           * Otherwise, if the inference variable is not contained within the set to be
           * captured we search for a non-proper equality which only mentions inference
           * variables which *are* in the set to be captured.
           * 
           * The wider purpose of this is to try to ensure that inference variables in the
           * upper bound may be substituted with captures wherever possible, such that the
           * bound is ultimately proper.
           * 
           * TODO may need to rethink approach to cases like the recent compiler-dev issue
           */
          TypeMirror replacement;
          if (inferenceVariableBounds.getInstantiation().isPresent()) {
            replacement = inferenceVariableBounds.getInstantiation().get();
          } else if (!types.contains(i)) {
            replacement = inferenceVariableBounds
                .getEqualities()
                .filter(types::contains)
                .findAny()
                .orElse(
                    inferenceVariableBounds
                        .getEqualities()
                        .filter(
                            equality -> InferenceVariable
                                .getMentionedBy(equality)
                                .allMatch(types::contains))
                        .findAny()
                        .orElseThrow(
                            () -> new ReflectionException(
                                REFLECTION_PROPERTIES.cannotFindSubstitution(i))));
          } else {
            replacement = i;
          }

          return replacement;
        });
  }
}
