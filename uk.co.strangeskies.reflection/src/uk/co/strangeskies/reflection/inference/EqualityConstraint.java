package uk.co.strangeskies.reflection.inference;

import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;

import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import uk.co.strangeskies.reflection.TypesOLD;
import uk.co.strangeskies.reflection.inference.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.model.ExtendedTypes;

public class EqualityConstraint extends ConstraintFormula {
  public EqualityConstraint(Kind kind, TypeMirror from, TypeMirror to) {
    super(kind, from, to);
  }

  @Override
  protected void reduceImpl(BoundSet bounds) {
    ExtendedTypes types = bounds.getTypes();

    if (getFrom() instanceof WildcardType && getTo() instanceof WildcardType) {
      /*
       * A constraint formula of the form ‹S = T›, where S and T are type arguments
       * (§4.5.1), is reduced as follows:
       */
      WildcardType from = (WildcardType) getFrom();
      WildcardType to = (WildcardType) getTo();

      if (from.getSuperBound() == null) {
        if (from.getExtendsBound() == null) {
          if (to.getSuperBound() == null) {
            if (to.getExtendsBound() == null) {
              /*
               * If S has the form ? and T has the form ?, the constraint reduces to true.
               */
              return;
            } else {
              /*
               * If S has the form ? and T has the form ? extends T', the constraint reduces
               * to ‹Object = T'›.
               */
              isObjectConstraint(to.getExtendsBound(), bounds);
            }
          }
        } else if (to.getSuperBound() == null) {
          if (to.getExtendsBound() == null) {
            /*
             * If S has the form ? extends S' and T has the form ?, the constraint reduces
             * to ‹S' = Object›.
             */
            isObjectConstraint(from.getExtendsBound(), bounds);
          } else {
            /*
             * If S has the form ? extends S' and T has the form ? extends T', the
             * constraint reduces to ‹S' = T'›.
             */
            constraintFormula(Kind.EQUALITY, from.getExtendsBound(), to.getExtendsBound())
                .reduceInPlace(bounds);
          }
        }
      } else if (to.getSuperBound() != null) {
        /*
         * If S has the form ? super S' and T has the form ? super T', the constraint
         * reduces to ‹S' = T'›.
         */
        constraintFormula(Kind.EQUALITY, from.getSuperBound(), to.getSuperBound())
            .reduceInPlace(bounds);
      } else {
        /*
         * Otherwise, the constraint reduces to false.
         */
        fail(bounds);
      }
    } else {
      /*
       * A constraint formula of the form ‹S = T›, where S and T are types, is reduced
       * as follows:
       */
      if (!types.isSameType(getFrom(), getTo())) {
        /*
         * If S and T are proper types, the constraint reduces to true if S is the same
         * as T (§4.3.4), and false otherwise.
         */
        if (bounds.isProperType(getFrom()) && bounds.isProperType(getTo())) {
          fail(bounds);

        } else if (getFrom() instanceof InferenceVariable) {
          /*
           * Otherwise, if S is an inference variable, α, the constraint reduces to the
           * bound α = T.
           */
          bounds.incorporateEquality(getFrom(), getTo());
        } else if (getTo() instanceof InferenceVariable) {
          /*
           * Otherwise, if T is an inference variable, α, the constraint reduces to the
           * bound S = α.
           */
          bounds.incorporateEquality(getFrom(), getTo());
        } else if ((getFrom() instanceof Class<?> && ((Class<?>) getFrom()).isArray())
            && (getTo() instanceof Class<?> && ((Class<?>) getTo()).isArray())) {
          /*
           * Otherwise, if S and T are array types, S'[] and T'[], the constraint reduces
           * to ‹S' = T'›.
           */
          constraintFormula(
              Kind.EQUALITY,
              TypesOLD.getComponentType(getFrom()),
              TypesOLD.getComponentType(getTo())).reduceInPlace(bounds);
        } else if (TypesOLD.getErasedType(getFrom()).equals(TypesOLD.getErasedType(getTo()))) {
          /*
           * Otherwise, if S and T are class or interface types with the same erasure,
           * where S has type arguments B1, ..., Bn and T has type arguments A1, ..., An,
           * the constraint reduces to the following new constraints: for all i (1 ≤ i ≤
           * n), ‹Bi = Ai›.
           */
          TypeMirror finalFrom = getFrom();
          TypeMirror finalTo = getTo();
          if (getFrom() instanceof ParameterizedType) {
            if (getTo() instanceof ParameterizedType) {
              Iterator<TypeMirror> fromArguments = getAllTypeArguments(
                  (ParameterizedType) finalFrom).map(Entry::getValue).iterator();

              getAllTypeArguments((ParameterizedType) finalTo)
                  .map(Entry::getValue)
                  .map(
                      toArgument -> constraintFormula(
                          Kind.EQUALITY,
                          fromArguments.next(),
                          toArgument))
                  .forEach(c -> c.reduceInPlace(bounds));
            } else {
              fail(bounds);
            }
          } else if (getTo() instanceof ParameterizedType) {
            fail(bounds);
          }
        } else {
          /*
           * Otherwise, the constraint reduces to false.
           */
          fail(bounds);
        }
      }
    }
  }
}
