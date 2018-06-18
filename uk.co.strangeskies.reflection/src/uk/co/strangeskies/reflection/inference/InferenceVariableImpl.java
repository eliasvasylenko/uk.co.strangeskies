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

import static java.util.Collections.emptyList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

/**
 * <p>
 * An inference variable can be thought of as a placeholder for an
 * <em>instantiation</em> of a {@link TypeVariable} of which we do not yet know
 * the exact type. An {@link InferenceVariableImpl} alone has no bounds or type
 * information attached to it. Instead, typically, they are contained within the
 * context of one or more {@link BoundSet}s, which will track bounds on a set of
 * inference variables such they their exact type can ultimately be inferred.
 * 
 * 
 * @author Elias N Vasylenko
 */
public class InferenceVariableImpl implements InferenceVariable {
  private static final AtomicLong COUNTER = new AtomicLong();

  private final String name;
  private final long number;

  /**
   * Create a new inference variable with a basic generated name, which is
   * contained within this bound set.
   */
  public InferenceVariableImpl() {
    this("INF");
  }

  /**
   * Create a new inference variable with the given name.
   * 
   * @param name
   *          A name to assign to a new inference variable.
   */
  public InferenceVariableImpl(String name) {
    this.name = name;
    number = COUNTER.incrementAndGet();
  }

  /**
   * @return The given name of the inference variable.
   */
  public String getName() {
    return name;
  }

  /**
   * @return The internally assigned number which uniquely distinguishes the
   *         inference variable from others with the same name.
   */
  public long getNumber() {
    return number;
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return emptyList();
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return (A[]) Array.newInstance(annotationType, 0);
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.OTHER;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitUnknown(this, p);
  }

  @Override
  public String toString() {
    return name + "#" + number;
  }
}
