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

import static uk.co.strangeskies.log.Log.discardingLog;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.ObservableValue;

/**
 * This interface represents a simple but powerful system for
 * internationalization. Instances of this class provide automatic
 * implementations of sub-interfaces of {@link Properties} according to a
 * {@link Locale} setting, which delegate method invocations to be fetched from
 * {@link ResourceBundle resource bundles}.
 * 
 * <p>
 * A property accessor interface is an interface to provide an API over static
 * properties and localized text and data. Users should not implement this class
 * themselves, instead they should define sub-interfaces, allowing
 * implementations to be automatically provided by {@link PropertyLoader}.
 * <p>
 * A key is generated for each method based on the class and method name. The
 * key is generated according to the {@link Properties} used to load the
 * {@link Properties} instance.
 * <p>
 * Default and static methods will be invoked directly.
 * <p>
 * A {@link Properties} instance is {@link Observable} over changes to its
 * locale, with the instance itself being passed as the message to observers.
 * <p>
 * For an example of how to use this interface, users may wish to take a look at
 * the {@link PropertyLoaderProperties} interface.
 * 
 * @author Elias N Vasylenko
 */
public interface PropertyLoader {
  PropertyLoader DEFAULT_PROPERTY_LOADER = newPropertyLoader(LocaleProvider.getDefaultProvider());

  /**
   * @return the current locale of all localized texts implemented by this
   *         {@link PropertyLoader}
   */
  Locale getLocale();

  /**
   * @return an observable over changes to the locale
   */
  ObservableValue<Locale> locale();

  /**
   * Generate an implementing instance of the given accessor interface class,
   * according to the rules described by {@link Properties}.
   * 
   * @param <T>
   *          the type of the localization text accessor interface
   * @param accessor
   *          the sub-interface of {@link Properties} we wish to implement
   * @return an implementation of the accessor interface
   */
  <T> T getProperties(Class<T> accessor);

  /**
   * @return the properties associated directly with the property loader itself
   */
  PropertyLoaderProperties getProperties();

  /**
   * Generate an implementing instance of the given accessor interface class,
   * according to the rules described by {@link Properties}.
   * 
   * @param <T>
   *          the type of the localization text accessor interface
   * @param accessor
   *          the sub-interface of {@link Properties} we wish to implement
   * @return an implementation of the accessor interface
   */
  static <T> T getDefaultProperties(Class<T> accessor) {
    return DEFAULT_PROPERTY_LOADER.getProperties(accessor);
  }

  /**
   * Get a simple {@link PropertyLoader} implementation over the given locale
   * provider.
   * 
   * @param provider
   *          a provider to establish a locale setting
   * @return a {@link PropertyLoader} for the given locale
   */
  static PropertyLoader newPropertyLoader(LocaleProvider provider) {
    return newPropertyLoader(provider, discardingLog());
  }

  /**
   * Get a simple {@link PropertyLoader} implementation over the given locale
   * provider.
   * 
   * @param provider
   *          a provider to establish a locale setting
   * @param log
   *          a log for localization information
   * @return a {@link PropertyLoader} for the given locale
   */
  static PropertyLoader newPropertyLoader(LocaleProvider provider, Log log) {
    return new PropertyLoaderImpl(provider, log);
  }
}
