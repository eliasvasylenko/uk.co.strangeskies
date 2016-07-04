package uk.co.strangeskies.text.properties;

import static java.util.Collections.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

public class PropertyResourceImpl implements PropertyResource {
	private final PropertyResourceStrategy strategy;
	private final PropertyAccessorConfiguration<?> configuration;
	private final Set<ResourceBundleDescriptor> resources;
	private final MultiMap<Locale, ResourceBundle, List<ResourceBundle>> localizedResourceBundles;

	/**
	 * Create a resource bundle with the given initial locale.
	 * 
	 * @param strategy
	 *          the strategy responsible for initialising this resource
	 * @param configuration
	 *          the resource locations
	 */
	protected PropertyResourceImpl(PropertyResourceStrategy<?> strategy, PropertyAccessorConfiguration<?> configuration) {
		this.strategy = strategy;
		this.configuration = configuration;
		localizedResourceBundles = new MultiHashMap<>(ArrayList::new);

		resources = new LinkedHashSet<>(getResources(configuration));

		if (getResourceBundle(Locale.ROOT).isEmpty()) {
			throw missingResourcesException();
		}
	}

	private MissingResourceException missingResourcesException() {
		return new MissingResourceException(
				"Cannot find resources for any of " + resources + " for " + configuration.getAccessor(),
				configuration.toString(), "");
	}

	@Override
	public PropertyResourceStrategy<?> getStrategy() {
		return strategy;
	}

	@Override
	public PropertyAccessorConfiguration<?> getConfiguration() {
		return configuration;
	}

	@Override
	public Set<String> getKeys(Locale locale) {
		Set<String> keys = new LinkedHashSet<>();

		for (ResourceBundle bundle : getResourceBundle(locale)) {
			keys.addAll(list(bundle.getKeys()));
		}

		return keys;
	}

	@Override
	public String getValue(String key, Locale locale) {
		for (ResourceBundle bundle : getResourceBundle(locale)) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {}
		}

		throw missingResourcesException();
	}

	protected synchronized List<ResourceBundle> getResourceBundle(Locale locale) {
		if (localizedResourceBundles.containsKey(locale)) {
			return localizedResourceBundles.get(locale);
		} else {
			List<ResourceBundle> resourceBundles = localizedResourceBundles.getCollection(locale);

			for (ResourceBundleDescriptor resource : resources) {
				try {
					resourceBundles.add(ResourceBundle.getBundle(resource.getLocation(), locale, resource.getClassLoader()));
				} catch (MissingResourceException e) {}
			}

			return resourceBundles;
		}
	}

	protected List<ResourceBundleDescriptor> getResources(PropertyAccessorConfiguration<?> accessorConfiguration) {
		String resource = accessorConfiguration.getConfiguration().resource();

		if (resource.equals(PropertyConfiguration.UNSPECIFIED_RESOURCE)) {
			resource = getDefaultResource(accessorConfiguration.getAccessor());
		}

		return Arrays.asList(new ResourceBundleDescriptor(accessorConfiguration.getAccessor().getClassLoader(), resource));
	}

	protected String getDefaultResource(Class<?> accessor) {
		return PropertyAccessor.removePropertiesPostfix(accessor.getName());
	}
}
