package uk.co.strangeskies.text.properties;

public class ResourceBundleDescriptor {
	private final ClassLoader classLoader;
	private final String location;

	public ResourceBundleDescriptor(ClassLoader classLoader, String location) {
		this.classLoader = classLoader;
		this.location = location;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String getLocation() {
		return location;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ResourceBundleDescriptor))
			return false;

		ResourceBundleDescriptor that = (ResourceBundleDescriptor) obj;

		return this.classLoader.equals(that.classLoader) && this.location.equals(that.location);
	}

	@Override
	public int hashCode() {
		return classLoader.hashCode() ^ location.hashCode();
	}

	@Override
	public String toString() {
		return location;
	}
}
