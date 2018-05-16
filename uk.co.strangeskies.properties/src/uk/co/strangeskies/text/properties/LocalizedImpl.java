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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import uk.co.strangeskies.observable.ObservablePropertyImpl;
import uk.co.strangeskies.observable.ObservableValue;
import uk.co.strangeskies.observable.Observer;

/*
 * Implementation of localized property
 */
class LocalizedImpl<A> extends ObservablePropertyImpl<String>
    implements LocalizedString, Observer<Locale> {
  private final PropertyAccessorDelegate<A> propertyAccessorDelegate;

  private final String key;
  private final List<Object> arguments;
  private final Map<Locale, String> cache;

  public LocalizedImpl(
      PropertyAccessorDelegate<A> propertyAccessorDelegate,
      String key,
      List<?> arguments) {
    super(new IllegalStateException("Locale has failed to initialize"));

    this.propertyAccessorDelegate = propertyAccessorDelegate;

    this.key = key;
    this.arguments = new ArrayList<>(arguments);
    this.cache = new ConcurrentHashMap<>();

    locale().weakReference().observe(this);
    updateText(locale().get());
  }

  private synchronized void updateText(Locale locale) {
    set(get(locale));
  }

  @Override
  public String toString() {
    return get().toString();
  }

  @Override
  public synchronized String get() {
    return super.get();
  }

  @Override
  public String get(Locale locale) {
    AnnotatedType annotatedStringType;
    try {
      annotatedStringType = getClass().getDeclaredField("key").getAnnotatedType();
    } catch (NoSuchFieldException | SecurityException e) {
      throw new AssertionError();
    }

    return cache.computeIfAbsent(locale, l -> {
      return (String) this.propertyAccessorDelegate
          .parseValueString(annotatedStringType, key, locale)
          .apply(arguments);
    });
  }

  @Override
  public void onNext(Locale locale) {
    updateText(locale);
  }

  @Override
  public ObservableValue<Locale> locale() {
    return this.propertyAccessorDelegate.getLoader().locale();
  }
}
