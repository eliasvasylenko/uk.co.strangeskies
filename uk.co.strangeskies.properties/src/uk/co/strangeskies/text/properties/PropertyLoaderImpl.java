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

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import uk.co.strangeskies.collection.computingmap.CacheComputingMap;
import uk.co.strangeskies.collection.computingmap.ComputingMap;
import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.log.Log.Level;
import uk.co.strangeskies.observable.ObservableValue;

class PropertyLoaderImpl implements PropertyLoader {
  private final ComputingMap<Class<?>, Object> localizationCache;

  private final LocaleProvider locale;
  private Log log;

  private final PropertyLoaderProperties text;

  /**
   * Create a new {@link PropertyLoader} instance for the given initial locale.
   * 
   * @param locale
   *          the initial locale
   * @param log
   *          the log for localization
   */
  public PropertyLoaderImpl(LocaleProvider locale, Log log) {
    localizationCache = new CacheComputingMap<>(c -> instantiateProperties(c), true);

    this.locale = locale;
    this.log = requireNonNull(log);

    PropertyLoaderProperties text;
    try {
      text = getProperties(PropertyLoaderProperties.class);
    } catch (Exception e) {
      text = new DefaultPropertyLoaderProperties();
    }
    this.text = text;

    if (log != null) {
      locale().observe(l -> {
        log.log(Level.INFO, getProperties().localeChanged(locale, getLocale()).toString());
      });
    }
  }

  @Override
  public PropertyLoaderProperties getProperties() {
    return text;
  }

  Log getLog() {
    return log;
  }

  @Override
  public Locale getLocale() {
    return locale.getLocale();
  }

  @Override
  public ObservableValue<Locale> locale() {
    return locale;
  }

  protected <T> T instantiateProperties(Class<T> source) {
    return new PropertyAccessorDelegate<>(
        this,
        PropertyResource.getBundle(source),
        getLog(),
        source).getProxy();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getProperties(Class<T> accessorConfiguration) {
    return (T) localizationCache.putGet(accessorConfiguration);
  }
}
