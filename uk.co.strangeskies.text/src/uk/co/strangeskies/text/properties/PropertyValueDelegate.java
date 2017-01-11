package uk.co.strangeskies.text.properties;

import static uk.co.strangeskies.text.properties.PropertyConfiguration.UNSPECIFIED_KEY_SPLIT_STRING;
import static uk.co.strangeskies.text.properties.PropertyResourceBundle.removePropertiesPostfix;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import uk.co.strangeskies.text.CamelCaseFormatter;
import uk.co.strangeskies.text.CamelCaseFormatter.UnformattingCase;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;
import uk.co.strangeskies.text.properties.PropertyLoaderImpl.MethodSignature;

class PropertyValueDelegate<A> {
	private final PropertyAccessorDelegate<A> accessorDelegate;
	private final Function<List<?>, Object> valueProvider;

	public PropertyValueDelegate(PropertyAccessorDelegate<A> propertyAccessorDelegate, MethodSignature signature) {
		this.accessorDelegate = propertyAccessorDelegate;
		PropertyAccessorConfiguration<A> source;
		PropertyConfiguration configuration = signature.method().getAnnotation(PropertyConfiguration.class);
		if (configuration != null) {
			source = propertyAccessorDelegate.getSource().derive(configuration);
		} else {
			source = propertyAccessorDelegate.getSource();
		}

		String key = getKey(source, signature);
		AnnotatedType propertyType = signature.method().getAnnotatedReturnType();
		Class<?> propertyClass = getRawType(propertyType.getType());

		if (signature.method().isAnnotationPresent(Nested.class)) {
			PropertyAccessorConfiguration<?> nestedConfiguration = accessorDelegate
					.getPropertiesConfigurationUnsafe(propertyClass)
					.derive(signature.method().getAnnotation(PropertyConfiguration.class));

			valueProvider = arguments -> accessorDelegate.getLoader().getProperties(nestedConfiguration);

		} else if (Localized.class.equals(propertyClass)) {
			valueProvider = arguments -> new LocalizedImpl<>(accessorDelegate, source, key, propertyType, arguments);

		} else {
			valueProvider = accessorDelegate.parseValueString(source, propertyType, key, Locale.ROOT);
		}
	}

	private String getKey(PropertyAccessorConfiguration<A> source, MethodSignature signature) {
		String key = source.getConfiguration().key();
		if (key.equals(PropertyConfiguration.UNSPECIFIED_KEY)) {
			key = PropertyConfiguration.UNQUALIFIED_SLASHED;
		}

		Object[] substitution = new Object[3];
		substitution[0] = formatKeyComponent(source, source.getAccessor().getPackage().getName());

		Class<?> s = source.getAccessor();
		String b = s.getSimpleName();
		String a = removePropertiesPostfix(b);
		substitution[1] = formatKeyComponent(source, a);
		substitution[2] = formatKeyComponent(source, signature.method().getName());

		return String.format(key, substitution);
	}

	private Object formatKeyComponent(PropertyAccessorConfiguration<A> source, String component) {
		String splitString = source.getConfiguration().keySplitString();
		if (!splitString.equals("") && !splitString.equals(UNSPECIFIED_KEY_SPLIT_STRING)) {
			component = new CamelCaseFormatter(splitString, false, UnformattingCase.PRESERVED).unformat(component);
		}

		KeyCase keyCase = source.getConfiguration().keyCase();
		if (keyCase == KeyCase.LOWER) {
			component = component.toLowerCase();
		} else if (keyCase == KeyCase.UPPER) {
			component = component.toUpperCase();
		}

		return component;
	}

	public Object getValue(List<?> arguments) {
		return valueProvider.apply(arguments);
	}

	private Class<?> getRawType(Type propertyType) {
		if (propertyType instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) propertyType).getRawType();
		} else if (propertyType instanceof Class<?>) {
			return (Class<?>) propertyType;
		} else {
			return null;
		}
	}
}
