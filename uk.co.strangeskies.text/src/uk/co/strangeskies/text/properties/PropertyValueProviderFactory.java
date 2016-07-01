package uk.co.strangeskies.text.properties;

import static java.util.Optional.empty;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.text.parsing.Parser;

public interface PropertyValueProviderFactory {
	interface Aggregate {
		Optional<PropertyValueProvider<?>> getPropertyProvider(AnnotatedType exactType);
	}

	Optional<PropertyValueProvider<?>> getPropertyProvider(AnnotatedType exactType, Aggregate aggregate);

	static <C, T> PropertyValueProviderFactory over(Class<T> propertyClass, PropertyValueProvider<T> provider) {
		return new PropertyValueProviderFactory() {
			@Override
			public Optional<PropertyValueProvider<?>> getPropertyProvider(AnnotatedType exactType, Aggregate aggregate) {
				if (exactType.getType() instanceof Class<?> && exactType.getType().equals(propertyClass)) {
					return Optional.of(provider);
				} else {
					return empty();
				}
			}
		};
	}

	static <C, T> PropertyValueProviderFactory over(Class<T> propertyClass, Parser<C> getValue,
			BiFunction<C, List<?>, T> instantiate, Function<String, C> defaultValue) {
		return over(propertyClass, PropertyValueProvider.over(getValue, instantiate, defaultValue));
	}

	static <C, T> PropertyValueProviderFactory over(Class<T> propertyClass, Parser<C> getValue,
			BiFunction<C, List<?>, T> instantiate) {
		return over(propertyClass, PropertyValueProvider.over(getValue, instantiate));
	}

	static <T> PropertyValueProviderFactory over(Class<T> propertyClass, Parser<T> getValue) {
		return over(propertyClass, PropertyValueProvider.over(getValue));
	}
}
