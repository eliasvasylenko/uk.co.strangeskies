/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi.text;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.osgi.LocaleManagerServiceConstants;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.text.LocaleManager;
import uk.co.strangeskies.utilities.text.LocaleProvider;

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
	public boolean addObserver(Consumer<? super Locale> observer) {
		return component.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Consumer<? super Locale> observer) {
		return component.removeObserver(observer);
	}

	@Override
	public Observable<Change<Locale>> changes() {
		return component.changes();
	}
}
