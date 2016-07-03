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

	private final Map<Class<? extends PropertyResourceStrategy<?>>, PropertyResourceStrategy<?>> resourceStrategies;
	private final Set<PropertyValueProviderFactory> propertyProviders;
	private final ComputingMap<PropertiesConfiguration<?>, Properties<?>> localizationCache;

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
		resourceStrategies = new ConcurrentHashMap<>();
		registerResourceStrategy(PropertyResourceBundleStrategy.getInstance());
		setDefaultResourceStrategy(PropertyResourceBundleStrategy.getInstance());

		propertyProviders = synchronizedSet(new LinkedHashSet<>());
		registerValueProvider(stringProvider());
		registerValueProvider(listProvider());
		registerValueProvider(optionalProvider());

		localizationCache = new CacheComputingMap<>(c -> instantiateProperties(c), true);

		this.locale = locale;
		this.log = log;

		PropertyLoaderProperties text;
		try {
			text = getProperties(PropertyLoaderProperties.class);
		} catch (Exception e) {
			text = new DefaultPropertyLoaderProperties();
		}
		this.text = text;

		if (log != null) {
			locale().addObserver(l -> {
				log.log(Level.INFO, getProperties().localeChanged(locale, getLocale()).toString());
			});
		}
	}

	private PropertyValueProviderFactory stringProvider() {
		return PropertyValueProviderFactory.over(String.class, Parser.matchingAll(),
				(s, a) -> String.format(s, a.toArray()), s -> getProperties().translationNotFoundSubstitution(s));
	}

	private PropertyValueProviderFactory listProvider() {
		return new PropertyValueProviderFactory() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> Optional<PropertyValueProvider<T>> getPropertyProvider(AnnotatedType exactType,
					PropertyLoader loader) {

				if (exactType instanceof AnnotatedParameterizedType
						&& ((ParameterizedType) exactType.getType()).getRawType().equals(List.class)) {

					AnnotatedType elementType = ((AnnotatedParameterizedType) exactType).getAnnotatedActualTypeArguments()[0];

					/*
					 * TODO proper exception
					 */
					return of((PropertyValueProvider<T>) PropertyValueProvider.over(Parser.list(
							loader.getValueProvider(elementType).orElseThrow(() -> new RuntimeException()).getParser(), "\\s*,\\s*"),

							(list, arguments) -> list.stream().map(element -> element.instantiate(arguments))
									.collect(Collectors.toList()),

							k -> Collections.emptyList()));

				} else {
					return empty();
				}
			}
		};
	}

	private PropertyValueProviderFactory optionalProvider() {
		return new PropertyValueProviderFactory() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> Optional<PropertyValueProvider<T>> getPropertyProvider(AnnotatedType exactType,
					PropertyLoader loader) {

				if (exactType instanceof AnnotatedParameterizedType
						&& ((ParameterizedType) exactType.getType()).getRawType().equals(Optional.class)) {

					AnnotatedType elementType = ((AnnotatedParameterizedType) exactType).getAnnotatedActualTypeArguments()[0];

					return loader.getValueProvider(elementType)
							.<PropertyValueProvider<T>>map(p -> new PropertyValueProvider<T>() {
								@Override
								public Parser<PropertyValue<T>> getParser() {
									Parser<PropertyValue<T>> optionalParser = p.getParser()
											.transform(v -> a -> (T) Optional.ofNullable(v.instantiate(a)));

									return optionalParser.orElse(a -> (T) Optional.empty());
								}

								@Override
								public Optional<PropertyValue<T>> getDefault(String keyString) {
									return Optional.of(arguments -> (T) Optional.empty());
								};
							});

				} else {
					return empty();
				}
			}
		};
	}

	@Override
	public PropertyLoaderProperties getProperties() {
		return text;
	}

	public Log getLog() {
		return new Log() {
			@Override
			public void log(Level level, String message) {
				if (log != null)
					log.log(level, message);
			}

			@Override
			public void log(Level level, String message, Throwable exception) {
				if (log != null)
					log.log(level, message, exception);
			}
		};
	}

	@Override
	public Locale getLocale() {
		return locale.getLocale();
	}

	@Override
	public ObservableValue<Locale> locale() {
		return locale;
	}

	protected <T extends Properties<T>> T instantiateProperties(PropertiesConfiguration<T> source) {
		return new PropertiesDelegate<>(this, getLog(), source).copy();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Properties<T>> T getProperties(PropertiesConfiguration<T> accessorConfiguration) {
		return (T) localizationCache.putGet(accessorConfiguration);
	}

	@Override
	public boolean registerValueProvider(PropertyValueProviderFactory propertyProvider) {
		return propertyProviders.add(propertyProvider);
	}

	@Override
	public boolean unregisterValueProvider(PropertyValueProviderFactory propertyProvider) {
		return propertyProviders.remove(propertyProvider);
	}

	@Override
	public List<PropertyValueProviderFactory> getValueProviders() {
		return new ArrayList<>(propertyProviders);
	}

	@Override
	public Optional<PropertyValueProvider<?>> getValueProvider(AnnotatedType type) {
		PropertyValueProviderFactory aggregateProvider = new PropertyValueProviderFactory() {
			@Override
			public <T> Optional<PropertyValueProvider<T>> getPropertyProvider(AnnotatedType exactType,
					PropertyLoader loader) {
				List<PropertyValueProvider<?>> providers = getValueProviders().stream()
						.map(p -> p.getPropertyProvider(exactType, loader)).filter(Optional::isPresent).map(Optional::get)
						.collect(Collectors.toList());

				if (!providers.isEmpty()) {
					return Optional.of(new PropertyValueProvider<T>() {
						@SuppressWarnings("unchecked")
						@Override
						public Parser<PropertyValue<T>> getParser() {
							return providers.stream().map(PropertyValueProvider::getParser).reduce(Parser::orElse).get()
									.transform(v -> (PropertyValue<T>) v);
						}

						@SuppressWarnings("unchecked")
						@Override
						public Optional<PropertyValue<T>> getDefault(String keyString) {
							return providers.stream().map(p -> p.getDefault(keyString)).filter(Optional::isPresent).findFirst()
									.flatMap(Function.identity()).map(v -> (PropertyValue<T>) v);
						}
					});
				} else {
					return Optional.empty();
				}
			}
		};

		return aggregateProvider.getPropertyProvider(type, this).map(p -> (PropertyValueProvider<?>) p);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PropertyResourceStrategy<T>> void setDefaultResourceStrategy(T strategy) {
		resourceStrategies.put((Class<PropertyResourceStrategy<?>>) (Class<?>) PropertyResourceStrategy.class, strategy);

	}

	@Override
	public <T extends PropertyResourceStrategy<T>> boolean registerResourceStrategy(T strategy) {
		return resourceStrategies.putIfAbsent(strategy.strategyClass(), strategy) == null;
	}

	@Override
	public <T extends PropertyResourceStrategy<T>> boolean unregisterResourceStrategy(T strategy) {
		return resourceStrategies.remove(strategy.strategyClass()) != null;
	}

	@Override
	public Set<Class<? extends PropertyResourceStrategy<?>>> getResourceStrategies() {
		return Collections.unmodifiableSet(resourceStrategies.keySet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PropertyResourceStrategy<T>> T getResourceStrategy(Class<T> strategy) {
		return (T) resourceStrategies.computeIfAbsent(strategy, k -> {
			try {
				return strategy.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new PropertyLoaderException(getProperties().cannotInstantiateStrategy(strategy), e);
			}
		});
	}
}
