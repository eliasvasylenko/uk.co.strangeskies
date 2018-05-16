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
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

class PropertyValueDelegate<A> {
  private final PropertyAccessorDelegate<A> accessorDelegate;
  private final Function<List<?>, Object> valueProvider;

  public PropertyValueDelegate(
      PropertyAccessorDelegate<A> propertyAccessorDelegate,
      Method method) {
    this.accessorDelegate = propertyAccessorDelegate;
    Class<A> source = propertyAccessorDelegate.getSource();

    String key = getKey(source, method);
    AnnotatedType propertyType = method.getAnnotatedReturnType();
    Class<?> propertyClass = getRawType(propertyType.getType());

    if (method.isAnnotationPresent(Nested.class)) {
      valueProvider = arguments -> accessorDelegate.getLoader().getProperties(propertyClass);

    } else if (LocalizedString.class.equals(propertyClass)) {
      valueProvider = arguments -> new LocalizedImpl<>(accessorDelegate, key, arguments);

    } else {
      valueProvider = accessorDelegate.parseValueString(propertyType, key, Locale.ROOT);
    }
  }

  private String getKey(Class<A> source, Method method) {
    Key key = method.getAnnotation(Key.class);
    if (key == null) {
      key = source.getAnnotation(Key.class);
    }
    String keyString;
    if (key != null) {
      keyString = key.value();
    } else {
      keyString = Key.UNQUALIFIED_DOTTED;
    }

    Object[] substitution = new String[3];
    substitution[0] = source.getPackage().getName();
    substitution[1] = source.getSimpleName();
    substitution[2] = method.getName();

    return String.format(keyString, substitution);
  }

  public Object getValue(List<?> arguments) {
    return valueProvider.apply(arguments);
  }

  private Class<?> getRawType(Type propertyType) {
    if (propertyType instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) propertyType).getRawType();
    } else if (propertyType instanceof Class<?>) {
      return (Class<?>) propertyType;
    } else {
      return null;
    }
  }
}
