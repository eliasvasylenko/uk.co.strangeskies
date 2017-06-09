/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.provider;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.Observer;
import uk.co.strangeskies.text.properties.LocaleManager;
import uk.co.strangeskies.text.properties.LocaleProvider;

/**
 * A locale manager configurable via the config admin service.
 * 
 * @author Elias N Vasylenko
 */
@Component(configurationPid = LocaleManagerServiceConstants.CONFIGURATION_PID)
public class LocaleManagerService implements LocaleManager, LocaleProvider {
	private final LocaleManager component;
	@Reference
	ConfigurationAdmin configurationAdmin;

	/**
	 * Create empty manager
	 */
	public LocaleManagerService() {
		component = LocaleManager.getManager(Locale.getDefault());
	}

	@Override
	public Locale get() {
		return component.getLocale();
	}

	@Override
	public Locale set(Locale locale) {
		Locale previous = setImpl(locale);

		if (!previous.equals(locale)) {
			try {
				Configuration configuration = configurationAdmin
						.getConfiguration(LocaleManagerServiceConstants.CONFIGURATION_PID);

				Dictionary<String, Object> properties = configuration.getProperties();
				properties.put(LocaleManagerServiceConstants.LOCALE_KEY, locale.toLanguageTag());
				configuration.update(properties);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return previous;
	}

	private Locale setImpl(Locale locale) {
		return component.set(locale);
	}

	@Modified
	void update(Map<String, String> configuration) {
		if (configuration != null) {
			String locale = configuration.get(LocaleManagerServiceConstants.LOCALE_KEY);
			if (locale != null) {
				setImpl(Locale.forLanguageTag(locale));
			}
		}
	}

	@Override
	public boolean addObserver(Observer<? super Locale> observer) {
		return component.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Observer<? super Locale> observer) {
		return component.removeObserver(observer);
	}

	@Override
	public Observable<Change<Locale>> changes() {
		return component.changes();
	}
}
