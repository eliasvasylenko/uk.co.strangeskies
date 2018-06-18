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
package uk.co.strangeskies.reflection.inference;

import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.lang.model.util.AbstractTypeVisitor9;

import uk.co.strangeskies.reflection.ParameterizedTypes;

/**
 * <p>
 * An inference variable can be thought of as a placeholder for an
 * <em>instantiation</em> of a {@link TypeVariable} of which we do not yet know
 * the exact type. An {@link InferenceVariable} alone has no bounds or type
 * information attached to it. Instead, typically, they are contained within the
 * context of one or more {@link BoundSet}s, which will track bounds on a set of
 * inference variables such they their exact type can ultimately be inferred.
 * 
 * 
 * @author Elias N Vasylenko
 */
public interface InferenceVariable extends TypeMirror {
  /**
   * Create fresh {@link InferenceVariable}s for each parameter of the given type
   * - and each non-statically enclosing type thereof - which is a
   * {@link WildcardType}. New bounds based on the bounds of those wildcards, and
   * the bounds of the {@link TypeVariable}s they substitute, will be incorporated
   * into the given {@link BoundSet}, along with a {@link CaptureConversion} bound
   * representing this capture conversion. The process of capture conversion is
   * described in more detail in the Java 8 language specification.
   * 
   * @param type
   *          A parameterized type whose wildcard type arguments, if present, we
   *          wish to capture as inference variables.
   * @param bounds
   *          The bound set we wish to create any fresh inference variables
   *          within, and incorporate any newly implied bounds into.
   * @return A new parameterized type derived from the given parameterized type,
   *         with any fresh {@link InferenceVariable}s substituted for the type
   *         arguments.
   */
  /*
   * Let G name a generic type declaration (§8.1.2, §9.1.2) with n type parameters
   * A1,...,An with corresponding bounds U1,...,Un.
   */
  public static DeclaredType captureConversion(DeclaredType type, BoundSet bounds) {
    if (ParameterizedTypes.getAllTypeArguments(type).anyMatch(WildcardType.class::isInstance)) {
      /*
       * There exists a capture conversion from a parameterized type G<T1,...,Tn>
       * (§4.5) to a parameterized type G<S1,...,Sn>, where, for 1 ≤ i ≤ n :
       */

      CaptureConversion captureConversion = new CaptureConversion(type);

      bounds.withIncorporated().captureConversion(captureConversion);

      return captureConversion.getCaptureType();
    } else
      return type;
  }

  /**
   * Find all inference variables mentioned by a type, whether in any bounds,
   * parameters, array types, etc. recursively.
   * 
   * @param type
   *          The type in which to find inference variable mentions.
   * @return The inference variables mentioned by the given type.
   */
  public static Stream<InferenceVariable> getMentionedBy(TypeMirror type) {
    Set<InferenceVariable> inferenceVariables = new HashSet<>();

    new AbstractTypeVisitor9<Void, Void>() {
      @Override
      public Void visitUnknown(TypeMirror t, Void p) {
        if (t instanceof InferenceVariable) {
          inferenceVariables.add((InferenceVariable) t);
          return null;
        } else {
          return super.visitUnknown(type, p);
        }
      }

      @Override
      public Void visitPrimitive(PrimitiveType t, Void p) {
        return null;
      }

      @Override
      public Void visitNull(NullType t, Void p) {
        return null;
      }

      @Override
      public Void visitArray(ArrayType t, Void p) {
        t.getComponentType().accept(this, p);
        return null;
      }

      @Override
      public Void visitDeclared(DeclaredType t, Void p) {
        t.getEnclosingType().accept(this, p);
        t.getTypeArguments().forEach(v -> v.accept(this, p));
        return null;
      }

      @Override
      public Void visitError(ErrorType t, Void p) {
        return null;
      }

      @Override
      public Void visitTypeVariable(javax.lang.model.type.TypeVariable t, Void p) {
        return null;
      }

      @Override
      public Void visitWildcard(javax.lang.model.type.WildcardType t, Void p) {
        t.getExtendsBound().accept(this, p);
        t.getSuperBound().accept(this, p);
        return null;
      }

      @Override
      public Void visitExecutable(ExecutableType t, Void p) {
        /*
         * The executable type itself can't be parameterized, only the owning type,
         * therefore this is the only place which needs searching for inference
         * variables.
         */
        t.getReceiverType().accept(this, p);
        return null;
      }

      @Override
      public Void visitNoType(NoType t, Void p) {
        return null;
      }

      @Override
      public Void visitIntersection(IntersectionType t, Void p) {
        t.getBounds().forEach(b -> b.accept(this, p));
        return null;
      }

      @Override
      public Void visitUnion(UnionType t, Void p) {
        t.getAlternatives().forEach(a -> a.accept(this, p));
        return null;
      }
    }.visit(type);

    return inferenceVariables.stream();
  }

  /**
   * Determine whether a given type is proper.
   * 
   * @param type
   *          The type for which to determine properness.
   * @return True if the given type is proper, false otherwise.
   */
  public static boolean isProperType(TypeMirror type) {
    Set<InferenceVariable> inferenceVariables = new HashSet<>();

    return new AbstractTypeVisitor9<Boolean, Void>() {
      @Override
      public Boolean visitUnknown(TypeMirror t, Void p) {
        if (t instanceof InferenceVariable) {
          inferenceVariables.add((InferenceVariable) t);
          return false;
        }
        return super.visitUnknown(type, p);
      }

      @Override
      public Boolean visitPrimitive(PrimitiveType t, Void p) {
        return true;
      }

      @Override
      public Boolean visitNull(NullType t, Void p) {
        return true;
      }

      @Override
      public Boolean visitArray(ArrayType t, Void p) {
        return t.getComponentType().accept(this, p);
      }

      @Override
      public Boolean visitDeclared(DeclaredType t, Void p) {
        return t.getEnclosingType().accept(this, p)
            && t.getTypeArguments().stream().allMatch(v -> v.accept(this, p));
      }

      @Override
      public Boolean visitError(ErrorType t, Void p) {
        return true;
      }

      @Override
      public Boolean visitTypeVariable(javax.lang.model.type.TypeVariable t, Void p) {
        return true;
      }

      @Override
      public Boolean visitWildcard(javax.lang.model.type.WildcardType t, Void p) {
        return t.getExtendsBound().accept(this, p) && t.getSuperBound().accept(this, p);
      }

      @Override
      public Boolean visitExecutable(ExecutableType t, Void p) {
        /*
         * The executable type itself can't be parameterized, only the owning type,
         * therefore this is the only place which needs searching for inference
         * variables.
         */
        return t.getReceiverType().accept(this, p);
      }

      @Override
      public Boolean visitNoType(NoType t, Void p) {
        return true;
      }

      @Override
      public Boolean visitIntersection(IntersectionType t, Void p) {
        return t.getBounds().stream().allMatch(b -> b.accept(this, p));
      }

      @Override
      public Boolean visitUnion(UnionType t, Void p) {
        return t.getAlternatives().stream().allMatch(a -> a.accept(this, p));
      }
    }.visit(type);
  }
}
