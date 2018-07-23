package uk.co.strangeskies.reflection.inference;

import static javax.lang.model.type.TypeKind.ARRAY;
import static javax.lang.model.type.TypeKind.DECLARED;
import static uk.co.strangeskies.reflection.inference.ConstraintFormula.Kind.EQUALITY;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.ExtendedTypes;
import uk.co.strangeskies.reflection.model.TypeHierarchy;

public class LooseCompatibilityConstraint extends ConstraintFormula {
  public LooseCompatibilityConstraint(Kind kind, TypeMirror from, TypeMirror to) {
    super(kind, from, to);
  }

  @Override
  protected void reduceImpl(BoundSet bounds) {
    /*
     * A constraint formula of the form ‹S → T› is reduced as follows:
     */

    TypeMirror from = getFrom();

    ExtendedTypes types = bounds.getTypes();

    /*
     * TODO should this capture be the responsibility of the caller?:
     */
    if (from.getKind() == DECLARED) {
      if (bounds.isProperType(from)) {
        from = types.capture(from);
      } else {
        CaptureConversion capture = new CaptureConversion(types, (DeclaredType) from);
        bounds.incorporateCaptureConversion(capture);
        from = capture.getCaptureType();
      }
    }

    if (bounds.isProperType(from) && bounds.isProperType(getTo())) {
      /*
       * If S and T are proper types, the constraint reduces to true if S is
       * compatible in a loose invocation context with T (§5.3), and false otherwise.
       */
      if (!types.isAssignable(from, getTo())) {
        fail(bounds);
      }
    } else if (from.getKind().isPrimitive()) {
      /*
       * Otherwise, if S is a primitive type, let S' be the result of applying boxing
       * conversion (§5.1.7) to S. Then the constraint reduces to ‹S' → T›.
       */
      constraintFormula(
          Kind.LOOSE_COMPATIBILILTY,
          types.boxedClass((PrimitiveType) from).asType(),
          getTo()).reduceInPlace(bounds);
    } else if (getTo().getKind().isPrimitive()) {
      /*
       * Otherwise, if T is a primitive type, let T' be the result of applying boxing
       * conversion (§5.1.7) to T. Then the constraint reduces to ‹S = T'›.
       */
      constraintFormula(EQUALITY, from, types.boxedClass((PrimitiveType) getTo()).asType())
          .reduceInPlace(bounds);
    } else if (isUncheckedCompatibleOnly(types, from, getTo())) {
      /*
       * Otherwise, if T is a parameterized type of the form G<T1, ..., Tn>, and there
       * exists no type of the form G<...> that is a supertype of S, but the raw type
       * G is a supertype of S, then the constraint reduces to true.
       * 
       * Otherwise, if T is an array type of the form G<T1, ..., Tn>[]k, and there
       * exists no type of the form G<...>[]k that is a supertype of S, but the raw
       * type G[]k is a supertype of S, then the constraint reduces to true. (The
       * notation []k indicates an array type of k dimensions.)
       */
      return;
    } else {
      /*
       * Otherwise, the constraint reduces to ‹S <: T›.
       */
      constraintFormula(Kind.SUBTYPE, from, getTo()).reduceInPlace(bounds);
    }
  }

  private static boolean isUncheckedCompatibleOnly(
      ExtendedTypes types,
      TypeMirror from,
      TypeMirror to) {
    TypeMirror toRaw = types.erasure(to);
    TypeMirror fromRaw = types.erasure(from);

    if (to.getKind() == DECLARED && !types.isErased(to)) {
      return types.isAssignable(fromRaw, toRaw)
          && types.isErased(new TypeHierarchy(types, from).resolveSupertype((DeclaredType) toRaw));

    } else if (to.getKind() == ARRAY) {
      return from.getKind() == ARRAY && isUncheckedCompatibleOnly(
          types,
          ((ArrayType) from).getComponentType(),
          ((ArrayType) to).getComponentType());

    } else {
      return false;
    }
  }
}
