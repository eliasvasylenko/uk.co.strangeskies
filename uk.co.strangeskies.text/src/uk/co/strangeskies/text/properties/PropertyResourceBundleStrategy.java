package uk.co.strangeskies.text.properties;

public class PropertyResourceBundleStrategy implements PropertyResourceStrategy<PropertyResourceBundleStrategy> {
	private static final PropertyResourceBundleStrategy INSTANCE = new PropertyResourceBundleStrategy();

	private PropertyResourceBundleStrategy() {}

	@Override
	public Class<PropertyResourceBundleStrategy> strategyClass() {
		return PropertyResourceBundleStrategy.class;
	}

	@Override
	public PropertyResource getPropertyResourceBundle(PropertyAccessorConfiguration<?> resourceConfiguration) {
		return new PropertyResourceImpl(this, resourceConfiguration);
	}

	/**
	 * @return an instance of the strategy
	 */
	public static final PropertyResourceBundleStrategy getInstance() {
		return INSTANCE;
	}
}
