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

import java.util.Arrays;
import java.util.Locale;

import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ServiceScope;

import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.text.LocaleProvider;
import uk.co.strangeskies.utilities.text.LocalizedResourceBundle;
import uk.co.strangeskies.utilities.text.LocalizedText;
import uk.co.strangeskies.utilities.text.Localizer;

@Component(scope = ServiceScope.PROTOTYPE)
@SuppressWarnings("javadoc")
public class LocalizerService implements Localizer {
	private static final String DEFAULT_OSGI_LOCALIZATION_LOCATION = "OSGI-INF.l10n.bundle";
	private static final String OSGI_LOCALIZATION_HEADER = "Bundle-Localization";

	@Reference
	LocaleProvider provider;
	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	Log log;
	private Localizer component;

	private ClassLoader classLoader;
	private String osgiLocalizationLocation;

	/**
	 * For automatic instantiation by the OSGi service manager
	 */
	public LocalizerService() {}

	@Activate
	void activate(ComponentContext context) {
		classLoader = context.getUsingBundle().adapt(BundleWiring.class).getClassLoader();

		osgiLocalizationLocation = context.getUsingBundle().getHeaders().get(OSGI_LOCALIZATION_HEADER);
		if (osgiLocalizationLocation == null)
			osgiLocalizationLocation = DEFAULT_OSGI_LOCALIZATION_LOCATION;

		component = Localizer.getLocalizer(provider, new Log() {
			@Override
			public void log(Level level, String message) {
				if (log != null) {
					log.log(level, message);
				}
			}

			@Override
			public void log(Level level, String message, Throwable exception) {
				if (log != null) {
					log.log(level, message, exception);
				}
			}
		});
	}

	@Override
	public <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, ClassLoader classLoader,
			String... locations) {
		String[] extraLocations = Arrays.copyOf(locations, locations.length + 1);
		extraLocations[locations.length] = osgiLocalizationLocation;

		return component.getLocalization(accessor, classLoader, extraLocations);
	}

	@Override
	public <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, String... locations) {
		return getLocalization(accessor, classLoader, locations);
	}

	@Override
	public Locale getLocale() {
		return provider.getLocale();
	}

	@Override
	public Observable<Locale> locale() {
		return provider;
	}

	@Override
	public <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, LocalizedResourceBundle bundle) {
		return component.getLocalization(accessor, bundle);
	}
}
