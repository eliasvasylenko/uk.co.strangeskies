package uk.co.strangeskies.reflection.inference;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

public class ContainmentConstraint extends ConstraintFormula {
  public ContainmentConstraint(Kind kind, TypeMirror from, TypeMirror to) {
    super(kind, from, to);
  }

  @Override
  protected void reduceImpl(BoundSet bounds) {
    /*
     * A constraint formula of the form ‹S <= T›, where S and T are type arguments
     * (§4.5.1), is reduced as follows:
     */

    if (!(getTo() instanceof WildcardType)) {
      /*
       * If T is a type:
       */
      if (!(getFrom() instanceof WildcardType)) {
        /*
         * If S is a type, the constraint reduces to ‹S = T›.
         */
        constraintFormula(Kind.EQUALITY, getFrom(), getTo()).reduceInPlace(bounds);
      } else {
        /*
         * If S is a wildcard, the constraint reduces to false.
         */
        fail(bounds);
      }
    } else {
      WildcardType toWildcard = (WildcardType) getTo();

      if (toWildcard.getSuperBound() == null) {
        if (toWildcard.getExtendsBound() == null) {
          /*
           * If T is a wildcard of the form ?, the constraint reduces to true.
           */
          return;
        } else {
          /*
           * If T is a wildcard of the form ? extends T':
           */

          if (!(getFrom() instanceof WildcardType)) {
            /*
             * If S is a type, the constraint reduces to ‹S <: T'›.
             */
            constraintFormula(Kind.SUBTYPE, getFrom(), toWildcard.getExtendsBound())
                .reduceInPlace(bounds);
          } else {
            WildcardType from = (WildcardType) getFrom();

            if (from.getSuperBound() == null) {
              if (from.getExtendsBound() == null) {
                /*
                 * If S is a wildcard of the form ?, the constraint reduces to ‹Object <: T'›.
                 */
                isObjectConstraint(toWildcard.getExtendsBound(), bounds);
              } else {
                /*
                 * If S is a wildcard of the form ? extends S', the constraint reduces to ‹S' <:
                 * T'›.
                 */
                constraintFormula(
                    Kind.SUBTYPE,
                    from.getExtendsBound(),
                    toWildcard.getExtendsBound()).reduceInPlace(bounds);
              }
            } else {
              /*
               * If S is a wildcard of the form ? super S', the constraint reduces to ‹Object
               * = T'›.
               */
              isObjectConstraint(toWildcard.getExtendsBound(), bounds);
            }
          }
        }
      } else {
        /*
         * If T is a wildcard of the form ? super T':
         */
        TypeMirror toSuperBound = toWildcard.getSuperBound();

        if (!(getFrom() instanceof WildcardType)) {
          /*
           * If S is a type, the constraint reduces to ‹T' <: S›.
           */
          constraintFormula(Kind.SUBTYPE, toSuperBound, getFrom()).reduceInPlace(bounds);
        } else {
          WildcardType from = (WildcardType) getFrom();

          if (from.getSuperBound() != null) {
            /*
             * If S is a wildcard of the form ? super S', the constraint reduces to ‹T' <:
             * S'›.
             */
            constraintFormula(Kind.SUBTYPE, toSuperBound, from.getSuperBound())
                .reduceInPlace(bounds);
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
}
