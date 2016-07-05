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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import uk.co.strangeskies.text.properties.Properties;
import uk.co.strangeskies.text.properties.PropertyAccessorConfiguration;
import uk.co.strangeskies.text.properties.PropertyResource;
import uk.co.strangeskies.text.properties.PropertyResourceStrategy;
import uk.co.strangeskies.text.properties.ResourceBundleDescriptor;

@SuppressWarnings("javadoc")
public class OsgiPropertyResourceStrategy implements PropertyResourceStrategy<OsgiPropertyResourceStrategy> {
	private static final String DEFAULT_OSGI_LOCALIZATION_LOCATION = "OSGI-INF.l10n.bundle";
	private static final String OSGI_LOCALIZATION_HEADER = "Bundle-Localization";

	private Bundle usingBundle;

	public OsgiPropertyResourceStrategy(Bundle usingBundle) {
		this.usingBundle = usingBundle;
	}

	@Override
	public Class<OsgiPropertyResourceStrategy> strategyClass() {
		return OsgiPropertyResourceStrategy.class;
	}

	@Override
	public PropertyResource getPropertyResourceBundle(PropertyAccessorConfiguration<?> resourceConfiguration) {
		// TODO Auto-generated method stub
		return null;
	}

	private ResourceBundleDescriptor getOsgiResourceDescriptor(Bundle bundle) {
		ClassLoader classLoader = bundle.adapt(BundleWiring.class).getClassLoader();

		String location = bundle.getHeaders().get(OSGI_LOCALIZATION_HEADER);
		if (location == null)
			location = DEFAULT_OSGI_LOCALIZATION_LOCATION;

		return new ResourceBundleDescriptor(classLoader, location);
	}

	private Collection<? extends ResourceBundleDescriptor> getResources(List<? extends Class<?>> accessors,
			ResourceBundleDescriptor osgiLocalizationResource) {
		List<ResourceBundleDescriptor> resources = new ArrayList<>();

		for (Class<?> accessor : accessors) {
			String accessorResource = Properties.removePropertiesPostfix(accessor.getName());
			resources.add(new ResourceBundleDescriptor(osgiLocalizationResource.getClassLoader(), accessorResource));
		}
		resources.add(osgiLocalizationResource);

		return resources;
	}
}
