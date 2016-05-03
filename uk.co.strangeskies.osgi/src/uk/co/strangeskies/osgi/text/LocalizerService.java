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
import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ServiceScope;

import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.text.LocalizedText;
import uk.co.strangeskies.utilities.text.Localizer;

@Component(service = Localizer.class, scope = ServiceScope.PROTOTYPE)
@SuppressWarnings("javadoc")
public class LocalizerService extends Localizer {
	private static final String DEFAULT_OSGI_LOCALIZATION_LOCATION = "OSGI-INF/l10n/bundle";
	private static final String OSGI_LOCALIZATION_HEADER = "Bundle-Localization";
	private String osgiLocalizationLocation;

	@Override
	public void setLocale(Locale locale) {
		if (!locale.equals(getLocale())) {
			setLocaleImpl(locale);

			// TODO update config admin service
		}
	}

	private void setLocaleImpl(Locale locale) {
		super.setLocale(locale);
	}

	@Modified
	public void update(Map<String, String> configuration) {
		// TODO configure locale
		// setLocaleImpl(newLocale);
	}

	@Override
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void setLog(Log log) {
		super.setLog(log);
	}

	void unsetLog(Log log) {
		setLog(null);
	}

	@Activate
	void activate(ComponentContext context) {
		osgiLocalizationLocation = context.getUsingBundle().getHeaders().get(OSGI_LOCALIZATION_HEADER);
		if (osgiLocalizationLocation == null)
			osgiLocalizationLocation = DEFAULT_OSGI_LOCALIZATION_LOCATION;
	}

	@Override
	public <T extends LocalizedText<T>> T getLocalization(Class<T> accessor, ClassLoader classLoader,
			String... locations) {
		String[] extraLocations = Arrays.copyOf(locations, locations.length + 1);
		extraLocations[locations.length] = osgiLocalizationLocation;

		return super.getLocalization(accessor, classLoader, extraLocations);
	}
}
