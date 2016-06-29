package uk.co.strangeskies.text.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public class PropertyResourceBundleImpl extends PropertyResourceBundle {
	private static final String TEXT_POSTFIX = "Properties";

	private final List<ResourceBundle> bundles;

	protected PropertyResourceBundleImpl(Locale locale, PropertyResourceStrategy strategy,
			PropertyResourceConfiguration<?> configuration) {
		super(locale, strategy, configuration);

		bundles = new ArrayList<>();

		Set<PropertyResource> resources = new LinkedHashSet<>(getResources(configuration));
		for (PropertyResource resource : resources) {
			try {
				bundles.add(ResourceBundle.getBundle(resource.getLocation(), locale, resource.getClassLoader()));
			} catch (MissingResourceException e) {}
		}

		if (bundles.isEmpty()) {
			throw new MissingResourceException("Cannot find resources for " + locale + " in any of " + resources,
					configuration.toString(), "");
		}
	}

	protected List<PropertyResource> getResources(PropertyResourceConfiguration<?> accessorConfiguration) {
		String resource = accessorConfiguration.getConfiguration().resource();

		if (resource.equals(PropertyConfiguration.UNSPECIFIED_RESOURCE)) {
			resource = getDefaultResource(accessorConfiguration.getAccessor());
		}

		return Arrays.asList(new PropertyResource(accessorConfiguration.getAccessor().getClassLoader(), resource));
	}

	protected String getDefaultResource(Class<?> accessor) {
		String accessorName = accessor.getName();

		if (accessorName.endsWith(TEXT_POSTFIX) && !accessorName.equals(TEXT_POSTFIX)) {
			accessorName = accessorName.substring(0, accessorName.length() - TEXT_POSTFIX.length());
		}

		System.out.println(accessorName);

		return accessorName;
	}

	@Override
	public Enumeration<String> getKeys() {
		Set<String> keys = new LinkedHashSet<>();

		for (ResourceBundle bundle : bundles)
			keys.addAll(bundle.keySet());

		return Collections.enumeration(keys);
	}

	@Override
	protected String handleGetObject(String key) {
		for (ResourceBundle bundle : bundles) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {}
		}

		return null;
	}
}
