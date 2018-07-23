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

import static java.lang.String.format;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.ReflectionException;

/**
 * <p>
 * A constraint formula, as they are described in chapter 18 of that Java 8
 * language specification.
 * 
 * <p>
 * Roughly, a constraint formula describes an assertion of compatibility between
 * two types, with respect to a particular constraining relationship. This
 * relationship may be reduced into a number of secondary, tertiary, etc.
 * constraint formulae, and then ultimately into a number of bounds, which in
 * turn may be incorporated into a {@link BoundSet}.
 * 
 * @author Elias N Vasylenko
 */
public abstract class ConstraintFormula {
  /**
   * The kind of a constraint formula describes the nature of the constraint it
   * represents.
   * 
   * @author Elias N Vasylenko
   */
  public enum Kind {
  /**
   * A loose compatibility constraint implies that two types be compatible within
   * a loose invocation context.
   */
  LOOSE_COMPATIBILILTY,
  /**
   * A subtype constraint between two types implies that the first be assignable
   * to the second.
   */
  SUBTYPE,
  /**
   * A containment constraint between two types implies that one contains the
   * other.
   */
  CONTAINMENT,
  /**
   * An equality constraint between two types implies that they are exactly
   * identical.
   */
  EQUALITY
  }

  private final Kind kind;
  private final TypeMirror from, to;

  /**
   * @param kind
   *          the kind of the constraint formula to create
   * @param from
   *          the first type of the constraint formula
   * @param to
   *          the second type of the constraint formula
   */
  public ConstraintFormula(Kind kind, TypeMirror from, TypeMirror to) {
    this.kind = kind;
    this.from = from;
    this.to = to;
  }

  public static ConstraintFormula constraintFormula(Kind kind, TypeMirror from, TypeMirror to) {
    switch (kind) {
    case LOOSE_COMPATIBILILTY:
      return new LooseCompatibilityConstraint(kind, from, to);
    case EQUALITY:
      return new EqualityConstraint(kind, from, to);
    case SUBTYPE:
      return new SubtypeConstraint(kind, from, to);
    case CONTAINMENT:
      return new ContainmentConstraint(kind, from, to);
    default:
      throw new AssertionError();
    }
  }

  public Kind getKind() {
    return kind;
  }

  public TypeMirror getFrom() {
    return from;
  }

  public TypeMirror getTo() {
    return to;
  }

  @Override
  public String toString() {
    return kind + " between '" + from + "' and '" + to + "'";
  }

  /**
   * Creates a {@link ConstraintFormula} and reduces it into the given
   * {@link BoundSet}.
   * 
   * @param bounds
   *          the bound set to reduce the created constraint formula into
   * @return the constraint formula created
   */
  public BoundSet reduce(BoundSet bounds) {
    bounds = bounds.copyInternal();
    reduceInPlace(bounds);
    return bounds;
  }

  protected void reduceInPlace(BoundSet bounds) {
    try {
      logConstraint(this, bounds);

      reduceImpl(bounds);
    } catch (Exception e) {
      fail(bounds, e);
    }
  }

  protected abstract void reduceImpl(BoundSet bounds);

  private void logConstraint(ConstraintFormula constraintFormula, BoundSet boundSet) {
    // System.out.println(constraintFormula + " into '" + boundSet + "'.");
  }

  protected <T> T fail(BoundSet bounds) {
    throw fail(bounds, null);
  }

  protected ReflectionException fail(BoundSet bounds, Throwable t) {
    return new ReflectionException(
        format(
            "Cannot reduce %s constraint between %s and %s into bound set %s",
            kind,
            from,
            to,
            bounds),
        t);
  }

  protected void isObjectConstraint(TypeMirror type, BoundSet bounds) {
    if (type.getKind() != TypeKind.DECLARED || !((TypeElement) ((DeclaredType) type).asElement())
        .getQualifiedName()
        .contentEquals(Object.class.getName()))
      fail(bounds);
  }
}
