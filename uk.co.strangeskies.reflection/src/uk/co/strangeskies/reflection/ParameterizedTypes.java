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
package uk.co.strangeskies.reflection;

import java.util.stream.Stream;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

/**
 * A collection of utility methods relating to parameterized types.
 * 
 * @author Elias N Vasylenko
 */
public class ParameterizedTypes {
  private ParameterizedTypes() {}

  /**
   * This method retrieves a list of all type variables present on the given raw
   * type, as well as all type variables on any enclosing types recursively, in
   * the order encountered.
   *
   * @param rawType
   *          The class whose generic type parameters we wish to determine.
   * @return A list of all relevant type variables.
   */
  public static Stream<TypeVariable> getAllTypeParameters(DeclaredType rawType) {
    Stream<TypeVariable> typeParameters = Stream.empty();

    do {
      typeParameters = Stream
          .concat(
              ((TypeElement) rawType.asElement())
                  .getTypeParameters()
                  .stream()
                  .map(TypeParameterElement::asType)
                  .map(TypeVariable.class::cast),
              typeParameters);

      TypeMirror enclosingType = rawType.getEnclosingType();
      if (enclosingType.getKind() == TypeKind.DECLARED) {
        rawType = (DeclaredType) enclosingType;
      } else {
        rawType = null;
      }
    } while (rawType != null);

    return typeParameters;
  }

  /**
   * For a given parameterized type, we retrieve a mapping of all type variables
   * on its raw type, as given by {@link #getAllTypeParameters(DeclaredType)}, to
   * their arguments within the context of this type.
   *
   * @param type
   *          The type whose generic type arguments we wish to determine.
   * @return A mapping of all type variables to their arguments in the context of
   *         the given type.
   */
  public static Stream<TypeMirror> getAllTypeArguments(DeclaredType type) {
    Stream<TypeMirror> typeArguments = Stream.empty();

    do {
      typeArguments = Stream.concat(type.getTypeArguments().stream(), typeArguments);

      TypeMirror enclosingType = type.getEnclosingType();
      if (enclosingType.getKind() == TypeKind.DECLARED) {
        type = (DeclaredType) enclosingType;
      } else {
        type = null;
      }
    } while (type != null);

    return typeArguments;
  }
}
