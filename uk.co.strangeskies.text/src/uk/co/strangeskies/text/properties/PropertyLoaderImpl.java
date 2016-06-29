/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.text.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import static java.util.Collections.synchronizedSet;
import static java.util.Collections.unmodifiableSet;
import static uk.co.strangeskies.text.properties.PropertyProvider.over;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.text.parsing.Parser;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.ObservableValue;
import uk.co.strangeskies.utilities.collection.computingmap.CacheComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

class PropertyLoaderImpl implements PropertyLoader {
	static class MethodSignature {
		private final Method method;
		private final Class<?>[] type;

		public MethodSignature(Method method) {
			this.method = method;
			this.type = method.getParameterTypes();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof MethodSignature))
				return false;

			MethodSignature other = (MethodSignature) obj;

			return name().equals(other.name()) && Arrays.equals(type(), other.type());
		}

		public Method method() {
			return method;
		}

		public String name() {
			return method.getName();
		}

		public Class<?>[] type() {
			return type;
		}

		@Override
		public int hashCode() {
			return name().hashCode() ^ Arrays.hashCode(type());
		}
	}

	private final Set<PropertyProvider<?>> propertyProviders;
	private final ComputingMap<PropertyAccessorConfiguration<?>, Properties<?>> localizationCache;

	private final LocaleProvider locale;
	private Log log;

	private final PropertyLoaderProperties text;

	/**
	 * Create a new {@link PropertyLoader} instance for the given initial locale.
	 * 
	 * @param locale
	 *          the initial locale
	 * @param log
	 *          the log for localisation
	 */
	public PropertyLoaderImpl(LocaleProvider locale, Log log) {
		propertyProviders = synchronizedSet(new HashSet<>());

		localizationCache = new CacheComputingMap<>(this::instantiateProperties, true);

		this.locale = locale;
		this.log = log;

		text = getProperties(PropertyLoaderProperties.class);

		if (log != null) {
			locale().addObserver(l -> {
				log.log(Level.INFO, getText().localeChanged(locale, getLocale()).toString());
			});
		}

		registerProvider(stringProvider());
	}

	private PropertyProvider<String> stringProvider() {
		return over(String.class, Parser.matchingAll(), String::format, text::translationNotFoundSubstitution);
	}

	public PropertyLoaderProperties getText() {
		PropertyLoaderProperties text = this.text;

		/*
		 * This check if for the sticky situation that getText() is invoked during
		 * construction before this.text is assigned.
		 */
		if (text == null) {
			text = new DefaultPropertyLoaderProperties();
		}

		return text;
	}

	void log(Level level, String message) {
		if (log != null) {
			log.log(level, message);
		}
	}

	<E extends Throwable> E log(Level level, E exception) {
		if (log != null) {
			log.log(level, exception);
		}
		return exception;
	}

	<E extends Throwable> E log(Level level, String message, E exception) {
		if (log != null) {
			log.log(level, message, exception);
		}
		return exception;
	}

	@Override
	public Locale getLocale() {
		return locale.getLocale();
	}

	@Override
	public ObservableValue<Locale> locale() {
		return locale;
	}

	protected <T extends Properties<T>> T instantiateProperties(PropertyAccessorConfiguration<T> source) {
		return new PropertiesDelegate<>(this, source).copy();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Properties<T>> T getProperties(Class<T> accessor, PropertyConfiguration defaultConfiguration) {
		return (T) localizationCache.putGet(new PropertyAccessorConfiguration<>(accessor, defaultConfiguration));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Properties<T>> T getProperties(Class<T> accessor) {
		return (T) localizationCache.putGet(new PropertyAccessorConfiguration<>(accessor));
	}

	@Override
	public boolean registerProvider(PropertyProvider<?> propertyProvider) {
		return propertyProviders.add(propertyProvider);
	}

	@Override
	public boolean unregisterProvider(PropertyProvider<?> propertyProvider) {
		return propertyProviders.remove(propertyProvider);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<PropertyProvider<T>> getProviders(Class<T> propertyClass) {
		return propertyProviders.stream()
				/*
				 * Filter on assignability to the requested property class ...
				 */
				.filter(p -> propertyClass.isAssignableFrom(p.getPropertyClass())).map(p -> (PropertyProvider<T>) p)
				/*
				 * Sort with less specific matches first ...
				 */
				.sorted((a, b) -> (a.getPropertyClass().isAssignableFrom(b.getPropertyClass()) ? 1 : 0)
						- (b.getPropertyClass().isAssignableFrom(a.getPropertyClass()) ? 1 : 0))
				.collect(Collectors.toList());
	}

	@Override
	public Set<PropertyProvider<?>> getProviders() {
		return unmodifiableSet(propertyProviders);
	}

	public PropertyResourceStrategy getResourceStrategyInstance(Class<? extends PropertyResourceStrategy> strategy) {
		try {
			if (strategy.equals(PropertyConfiguration.UnspecifiedPropertyResourceStrategy.class)) {
				return DefaultPropertyResourceStrategy.getInstance();
			} else {
				return strategy.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new PropertyLoaderException(getText().cannotInstantiateStrategy(strategy), e);
		}
	}
}
