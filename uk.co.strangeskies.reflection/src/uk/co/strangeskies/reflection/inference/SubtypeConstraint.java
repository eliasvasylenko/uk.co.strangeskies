package uk.co.strangeskies.reflection.inference;

import static javax.lang.model.type.TypeKind.ARRAY;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.INTERSECTION;
import static javax.lang.model.type.TypeKind.NULL;
import static javax.lang.model.type.TypeKind.TYPEVAR;
import static javax.lang.model.type.TypeKind.WILDCARD;
import static uk.co.strangeskies.collection.stream.StreamUtilities.zip;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import uk.co.strangeskies.reflection.model.ExtendedTypes;
import uk.co.strangeskies.reflection.model.TypeHierarchy;

public class SubtypeConstraint extends ConstraintFormula {
  public SubtypeConstraint(Kind kind, TypeMirror from, TypeMirror to) {
    super(kind, from, to);
  }

  @Override
  protected void reduceImpl(BoundSet bounds) {
    /*
     * A constraint formula of the form ‹S <: T› is reduced as follows:
     */

    ExtendedTypes types = bounds.getTypes();

    if (bounds.isProperType(getFrom()) && bounds.isProperType(getTo())) {
      /*
       * If S and T are proper types, the constraint reduces to true if S is a subtype
       * of T (§4.10), and false otherwise.
       */
      if (!types.isSubtype(getFrom(), getFrom())) {
        fail(bounds);
      } else {
        return;
      }
    } else if (getFrom().getKind() == NULL) {
      /*
       * Otherwise, if S is the null type, the constraint reduces to true.
       */
      return;
    } else if (getTo().getKind() == NULL) {
      /*
       * Otherwise, if T is the null type, the constraint reduces to false.
       */
      fail(bounds);
    } else if (bounds.containsInferenceVariable(getFrom())) {
      /*
       * Otherwise, if S is an inference variable, α, the constraint reduces to the
       * bound α <: T.
       */
      bounds.incorporateSubtype(getFrom(), getTo());
    } else if (bounds.containsInferenceVariable(getTo())) {
      /*
       * Otherwise, if T is an inference variable, α, the constraint reduces to the
       * bound S <: α.
       */
      bounds.incorporateSubtype(getFrom(), getTo());
    } else {
      /*
       * Otherwise, the constraint is reduced according to the form of T:
       */
      if (getTo().getKind() == DECLARED) {
        if (!types.isErased(getTo())) {
          /*
           * If T is a parameterized class or interface type, or an inner class type of a
           * parameterized class or interface type (directly or indirectly), let A1, ...,
           * An be the type arguments of T. Among the supertypes of S, a corresponding
           * class or interface type is identified, with type arguments B1, ..., Bn.
           */
          DeclaredType rawType = (DeclaredType) types.erasure(getTo());
          if (!types.isAssignable(getFrom(), rawType)) {
            /*
             * If no such type exists, the constraint reduces to false.
             */
            fail(bounds);
          } else {
            DeclaredType fromParameterization = new TypeHierarchy(types, getFrom())
                .resolveSupertype(rawType);
            if (types.isErased(fromParameterization)) {
              /*
               * If no such type exists, the constraint reduces to false.
               */
              fail(bounds);

            } else {
              /*
               * Otherwise, the constraint reduces to the following new constraints: for all i
               * (1 ≤ i ≤ n), ‹Bi <= Ai›.
               */

              zip(
                  getAllTypeArguments(fromParameterization),
                  getAllTypeArguments((DeclaredType) getTo()))
                      .map(e -> constraintFormula(Kind.CONTAINMENT, e.getKey(), e.getValue()))
                      .forEach(c -> c.reduceInPlace(bounds));
            }
          }

        } else {
          /*
           * If T is any other class or interface type, then the constraint reduces to
           * true if T is among the supertypes of S, and false otherwise.
           */
          if (!types.isAssignable(getFrom(), getTo()))
            fail(bounds);
        }

      } else if (getTo().getKind() == ARRAY) {
        /*
         * If T is an array type, T'[], then among the supertypes of S that are array
         * types, a most specific type is identified, S'[] (this may be S itself).
         */
        TypeMirror fromComponent = findMostSpecificArrayComponentType(types, getFrom());
        if (fromComponent == null) {
          /*
           * If no such array type exists, the constraint reduces to false.
           */
          fail(bounds);
        } else {
          /*
           * Otherwise:
           */
          TypeMirror toComponent = ((ArrayType) getTo()).getComponentType();
          if (!fromComponent.getKind().isPrimitive() && !toComponent.getKind().isPrimitive()) {
            /*
             * - If neither S' nor T' is a primitive type, the constraint reduces to ‹S' <:
             * T'›.
             */
            constraintFormula(Kind.SUBTYPE, fromComponent, toComponent).reduceInPlace(bounds);
          } else {
            /*
             * - Otherwise, the constraint reduces to true if S' and T' are the same
             * primitive type, and false otherwise.
             */
            if (toComponent.getKind() != fromComponent.getKind()) {
              fail(bounds);
            }
          }
        }
      } else if (getTo().getKind() == TYPEVAR) {
        /*
         * If T is a type variable, there are three cases:
         */
        if (getFrom().getKind() == INTERSECTION && ((IntersectionType) getFrom())
            .getBounds()
            .stream()
            .anyMatch(f -> f.equals(getTo()))) {
          /*
           * - If S is an intersection type of which T is an element, the constraint
           * reduces to true.
           */
        } else if (((TypeVariable) getTo()).getLowerBound().getKind() != NULL) {
          /*
           * - Otherwise, if T has a lower bound, B, the constraint reduces to ‹S <: B›.
           */
          constraintFormula(Kind.SUBTYPE, getFrom(), ((TypeVariable) getTo()).getLowerBound())
              .reduceInPlace(bounds);
        } else {
          /*
           * - Otherwise, the constraint reduces to false.
           */
          fail(bounds);
        }
      } else if (getTo().getKind() == INTERSECTION) {
        /*
         * If T is an intersection type, I1 & ... & In, the constraint reduces to the
         * following new constraints: for all i (1 ≤ i ≤ n), ‹S <: Ii›.
         */
        for (TypeMirror typeComponent : ((IntersectionType) getTo()).getBounds())
          constraintFormula(Kind.SUBTYPE, getFrom(), typeComponent).reduceInPlace(bounds);
      } else {
        fail(bounds);
      }
    }
  }

  private TypeMirror findMostSpecificArrayComponentType(ExtendedTypes types, TypeMirror from) {
    if (from.getKind() == ARRAY) {
      return ((ArrayType) from).getComponentType();
    }

    if (from.getKind() == WILDCARD) {
      from = ((WildcardType) from).getExtendsBound();
    }

    if (from.getKind() == INTERSECTION) {
      // attempt to find most specific from candidates
      return ((IntersectionType) from)
          .getBounds()
          .stream()
          .filter(t -> types.erasure(t).getKind() == ARRAY)
          .map(t -> ((ArrayType) t).getComponentType())
          .reduce((a, b) -> types.isAssignable(a, b) ? a : types.isAssignable(b, a) ? b : null)
          .orElse(null);
    }

    return null;
  }
}
