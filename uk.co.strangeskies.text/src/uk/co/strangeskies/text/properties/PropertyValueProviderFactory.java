package uk.co.strangeskies.text.properties;

import static java.util.Optional.empty;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.text.parsing.Parser;

public interface PropertyValueProviderFactory {
	/**
	 * The method is generic over the return type to signify that the method is
	 * inherently unsafe, by forcing implementors to unsafe-cast the result.
	 * Implementors are responsible for ensuring the type T according to the
	 * returned object is a subtype of the given annotated type.
	 * 
	 * @param exactType
	 *          the type of the property provider
	 * @param loader
	 *          the property loader making the request
	 * @return a property provider over the given type
	 */
	<T> Optional<PropertyValueProvider<T>> getPropertyProvider(AnnotatedType exactType, PropertyLoader loader);

	static <C, T> PropertyValueProviderFactory over(Class<T> propertyClass, PropertyValueProvider<T> provider) {
		return new PropertyValueProviderFactory() {
			@SuppressWarnings("unchecked")
			@Override
			public <U> Optional<PropertyValueProvider<U>> getPropertyProvider(AnnotatedType exactType,
					PropertyLoader loader) {
				if (exactType.getType() instanceof Class<?> && exactType.getType().equals(propertyClass)) {
					return Optional.of((PropertyValueProvider<U>) provider);
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
