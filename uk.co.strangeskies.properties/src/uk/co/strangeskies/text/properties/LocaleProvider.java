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

import java.util.Locale;

import uk.co.strangeskies.observable.Disposable;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.ObservableValue;
import uk.co.strangeskies.observable.Observer;

/**
 * Management interface over and associate {@link PropertyLoader localiser
 * instance}, allowing the locale of that instance to be changed.
 * <p>
 * A locale manager is observable over changes to its locale.
 * 
 * @author Elias N Vasylenko
 */
public interface LocaleProvider extends ObservableValue<Locale> {
  /**
   * As returned by {@link #getDefaultProvider()}.
   */
  LocaleProvider DEFAULT_PROVIDER = new DefaultLocaleProvider();

  /**
   * @return the current locale
   */
  default Locale getLocale() {
    return get();
  }

  /**
   * Create a locale provider based on the system default locale, as returned by
   * {@link Locale#getDefault()}.
   * 
   * @return a locale manager initialized according to the system locale
   */
  static LocaleProvider getDefaultProvider() {
    return DEFAULT_PROVIDER;
  }
}

class DefaultLocaleProvider implements LocaleProvider {
  private final Locale locale;

  public DefaultLocaleProvider() {
    this.locale = Locale.getDefault();
  }

  @Override
  public Locale get() {
    return locale;
  }

  @Override
  public Observable<Change<Locale>> changes() {
    return Observable.empty();
  }

  @Override
  public Disposable observe(Observer<? super Locale> observer) {
    return Observable.empty().observe();
  }
}