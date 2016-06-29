package uk.co.strangeskies.text.properties;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.ObservableImpl;

public class GuardedPropertyLoaderProperties extends ObservableImpl<PropertyLoaderProperties>
		implements PropertyLoaderProperties {
	private final PropertyLoader loader;
	private PropertyLoaderProperties properties;

	public GuardedPropertyLoaderProperties(PropertyLoader loader) {
		this.loader = loader;
	}

	public PropertyLoaderProperties getGuardedProperties() {
		if (properties == null) {
			properties = new DefaultPropertyLoaderProperties();
			Consumer<PropertyLoaderProperties> consumer = this::fire;
			properties.addObserver(consumer);

			try {
				PropertyLoaderProperties properties = loader.getProperties(PropertyLoaderProperties.class);
				this.properties.removeObserver(consumer);
				this.properties = properties;
			} catch (Exception e) {
				/*
				 * TODO warn
				 */
			}
		}
		return properties;
	}

	@Override
	public Locale getLocale() {
		return loader.getLocale();
	}

	@Override
	public PropertyLoaderProperties copy() {
		return this;
	}

	@Override
	public Localized<String> mustBeInterface(Class<?> accessor) {
		return getGuardedProperties().mustBeInterface(accessor);
	}

	@Override
	public Localized<String> illegalReturnType(Type type, String key) {
		return getGuardedProperties().illegalReturnType(type, key);
	}

	@Override
	public String translationNotFoundSubstitution(String key) {
		return getGuardedProperties().translationNotFoundSubstitution(key);
	}

	@Override
	public Localized<String> translationNotFoundMessage(String key) {
		return getGuardedProperties().translationNotFoundMessage(key);
	}

	@Override
	public Localized<String> localeChanged(LocaleProvider manager, Locale locale) {
		return getGuardedProperties().localeChanged(manager, locale);
	}

	@Override
	public Localized<String> cannotInstantiateStrategy(Class<? extends PropertyResourceStrategy> strategy) {
		return getGuardedProperties().cannotInstantiateStrategy(strategy);
	}

}
