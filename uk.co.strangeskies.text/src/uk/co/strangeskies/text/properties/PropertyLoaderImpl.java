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
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
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

	private final Map<Class<? extends PropertyResourceStrategy>, PropertyResourceStrategy> resourceStrategies;
	private final Set<PropertyValueProviderFactory> propertyProviders;
	private final ComputingMap<PropertyResourceConfiguration<?>, Properties<?>> localizationCache;

	private final LocaleProvider locale;
	private Log log;

	private Function<String, String> translationNotFound;
	private final DefaultPropertyLoaderProperties defaultText;
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
		resourceStrategies = new ConcurrentHashMap<>();
		resourceStrategies.put(DefaultPropertyResourceStrategy.class, DefaultPropertyResourceStrategy.getInstance());
		propertyProviders = synchronizedSet(new LinkedHashSet<>());

		localizationCache = new CacheComputingMap<>(c -> instantiateProperties(c), true);

		this.locale = locale;
		this.log = log;

		defaultText = new DefaultPropertyLoaderProperties();
		text = getProperties(PropertyLoaderProperties.class);

		if (log != null) {
			locale().addObserver(l -> {
				log.log(Level.INFO, getText().localeChanged(locale, getLocale()).toString());
			});
		}

		registerProvider(listProvider());
		registerProvider(stringProvider());
	}

	protected DefaultPropertyLoaderProperties getDefaultText() {
		return defaultText;
	}

	private String translationNotFoundSubstitution(String string) {
		if (translationNotFound == null) {
			translationNotFound = defaultText::translationNotFoundSubstitution;
			try {
				translationNotFound = getText()::translationNotFoundSubstitution;
			} catch (Exception e) {}
		}
		return translationNotFound.apply(string);
	}

	private PropertyValueProviderFactory stringProvider() {
		return PropertyValueProviderFactory.over(String.class, Parser.matchingAll(),
				(s, a) -> String.format(s, a.toArray()), this::translationNotFoundSubstitution);
	}

	private PropertyValueProviderFactory listProvider() {
		return new PropertyValueProviderFactory() {
			@Override
			public Optional<PropertyValueProvider<?>> getPropertyProvider(AnnotatedType exactType, Aggregate aggregate) {

				if (exactType instanceof AnnotatedParameterizedType
						&& ((ParameterizedType) exactType.getType()).getRawType().equals(List.class)) {

					AnnotatedType elementType = ((AnnotatedParameterizedType) exactType).getAnnotatedActualTypeArguments()[0];

					return of(PropertyValueProvider.over(Parser.list(
							aggregate.getPropertyProvider(elementType).orElseThrow(() -> new RuntimeException()).getParser(),
							"\\s*,\\s*"),

							(list, arguments) -> list.stream().map(element -> element.instantiate(arguments))
									.collect(Collectors.toList()),

							k -> Collections.emptyList()));

				} else {
					return empty();
				}
			}
		};
	}

	public PropertyLoaderProperties getText() {
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

	protected <T extends Properties<T>> T instantiateProperties(PropertyResourceConfiguration<T> source) {
		return new PropertiesDelegate<>(this, source).copy();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Properties<T>> T getProperties(Class<T> accessor, PropertyConfiguration defaultConfiguration) {
		return (T) localizationCache.putGet(new PropertyResourceConfiguration<>(accessor, defaultConfiguration));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Properties<T>> T getProperties(Class<T> accessor) {
		return (T) localizationCache.putGet(new PropertyResourceConfiguration<>(accessor));
	}

	@Override
	public boolean registerProvider(PropertyValueProviderFactory propertyProvider) {
		return propertyProviders.add(propertyProvider);
	}

	@Override
	public boolean unregisterProvider(PropertyValueProviderFactory propertyProvider) {
		return propertyProviders.remove(propertyProvider);
	}

	@Override
	public List<PropertyValueProviderFactory> getProviders() {
		return new ArrayList<>(propertyProviders);
	}

	@Override
	public Optional<PropertyValueProvider<?>> getProvider(AnnotatedType type) {
		PropertyValueProviderFactory aggregateProvider = null; // TODO

		return aggregateProvider.getPropertyProvider(type, this::getProvider);
	}

	public PropertyResourceStrategy getResourceStrategyInstance(Class<? extends PropertyResourceStrategy> strategy) {
		try {
			if (strategy.equals(PropertyConfiguration.UnspecifiedPropertyResourceStrategy.class)) {
				return DefaultPropertyResourceStrategy.getInstance();
			} else if (resourceStrategies.containsKey(strategy)) {
				return resourceStrategies.get(strategy);
			} else {
				return strategy.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new PropertyLoaderException(getText().cannotInstantiateStrategy(strategy), e);
		}
	}
}
