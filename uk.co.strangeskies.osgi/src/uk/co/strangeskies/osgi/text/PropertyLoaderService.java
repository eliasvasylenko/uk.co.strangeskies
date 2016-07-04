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

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import uk.co.strangeskies.text.properties.LocaleProvider;
import uk.co.strangeskies.text.properties.PropertyAccessor;
import uk.co.strangeskies.text.properties.PropertyAccessorConfiguration;
import uk.co.strangeskies.text.properties.PropertyLoader;
import uk.co.strangeskies.text.properties.PropertyLoaderProperties;
import uk.co.strangeskies.text.properties.PropertyResourceStrategy;
import uk.co.strangeskies.text.properties.PropertyValueProvider;
import uk.co.strangeskies.text.properties.PropertyValueProviderFactory;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.ObservableValue;

@Component(scope = ServiceScope.PROTOTYPE)
@SuppressWarnings("javadoc")
public class PropertyLoaderService implements PropertyLoader {
	@Reference
	LocaleProvider provider;
	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	Log log;
	private PropertyLoader component;

	/**
	 * For automatic instantiation by the OSGi service manager
	 */
	public PropertyLoaderService() {}

	@Activate
	void activate(ComponentContext context) {
		component = PropertyLoader.getPropertyLoader(provider, new Log() {
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

		OsgiPropertyResourceStrategy osgiPropertyResourceStrategy = new OsgiPropertyResourceStrategy(
				context.getUsingBundle());

		component.setDefaultResourceStrategy(osgiPropertyResourceStrategy);
		component.registerResourceStrategy(osgiPropertyResourceStrategy);

		ServiceTracker<PropertyValueProviderFactory, Object> valueProviderTracker = new ServiceTracker<>(
				context.getUsingBundle().getBundleContext(), PropertyValueProviderFactory.class,
				new ServiceTrackerCustomizer<PropertyValueProviderFactory, Object>() {
					@Override
					public Object addingService(ServiceReference<PropertyValueProviderFactory> reference) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public void modifiedService(ServiceReference<PropertyValueProviderFactory> reference, Object service) {
						// TODO Auto-generated method stub

					}

					@Override
					public void removedService(ServiceReference<PropertyValueProviderFactory> reference, Object service) {
						// TODO Auto-generated method stub

					}
				});
		valueProviderTracker.open();
	}

	@Override
	public Locale getLocale() {
		return provider.getLocale();
	}

	@Override
	public ObservableValue<Locale> locale() {
		return provider;
	}

	@Override
	public boolean registerValueProvider(PropertyValueProviderFactory propertyProvider) {
		return component.registerValueProvider(propertyProvider);
	}

	@Override
	public boolean unregisterValueProvider(PropertyValueProviderFactory propertyProvider) {
		return component.unregisterValueProvider(propertyProvider);
	}

	@Override
	public List<PropertyValueProviderFactory> getValueProviders() {
		return component.getValueProviders();
	}

	@Override
	public Optional<PropertyValueProvider<?>> getValueProvider(AnnotatedType type) {
		return component.getValueProvider(type);
	}

	@Override
	public <T extends PropertyResourceStrategy<T>> void setDefaultResourceStrategy(T strategy) {
		component.setDefaultResourceStrategy(strategy);
	}

	@Override
	public <T extends PropertyResourceStrategy<T>> boolean registerResourceStrategy(T strategy) {
		return component.registerResourceStrategy(strategy);
	}

	@Override
	public <T extends PropertyResourceStrategy<T>> boolean unregisterResourceStrategy(T strategy) {
		return component.unregisterResourceStrategy(strategy);
	}

	@Override
	public Set<Class<? extends PropertyResourceStrategy<?>>> getResourceStrategies() {
		return component.getResourceStrategies();
	}

	@Override
	public <T extends PropertyResourceStrategy<T>> T getResourceStrategy(Class<T> strategy) {
		return component.getResourceStrategy(strategy);
	}

	@Override
	public <T extends PropertyAccessor<T>> T getProperties(Class<T> accessor) {
		return component.getProperties(accessor);
	}

	@Override
	public <T extends PropertyAccessor<T>> T getProperties(PropertyAccessorConfiguration<T> accessorConfiguration) {
		return component.getProperties(accessorConfiguration);
	}

	@Override
	public PropertyLoaderProperties getProperties() {
		return component.getProperties();
	}
}
