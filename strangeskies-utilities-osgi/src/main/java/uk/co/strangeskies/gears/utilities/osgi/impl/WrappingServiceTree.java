package uk.co.strangeskies.gears.utilities.osgi.impl;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import uk.co.strangeskies.gears.utilities.collection.SetMultiMap;

public class WrappingServiceTree {
	private final ServiceReference<?> serviceReference;
	private final WrappingServiceTreeNode root;

	WrappingServiceTree(ServiceReference<?> serviceReference,
			SetMultiMap<Class<?>, ManagedServiceWrapper<?>> wrappedServiceClasses) {
		this.serviceReference = serviceReference;
		root = new WrappingServiceTreeNode(serviceReference.getBundle()
				.getBundleContext().getService(serviceReference),
				getProperties(serviceReference));

		Set<Class<?>> classes = getClasses(serviceReference);

		SortedSet<ManagedServiceWrapper<?>> orderedServiceWrappers = new TreeSet<>(
				new ManagedServiceWrapperComparator());
		orderedServiceWrappers.addAll(wrappedServiceClasses.getAll(classes));

		Set<WrappingServiceTreeNode> workingSet = new HashSet<>();
		workingSet.add(root);

		WrappingServiceTreeNode wrappingService;
		for (ManagedServiceWrapper<?> serviceWrapper : orderedServiceWrappers)
			for (WrappingServiceTreeNode service : new HashSet<>(workingSet))
				if (service.isVisible()
						&& (wrappingService = service.wrap(serviceWrapper, classes)) != null)
					workingSet.add(wrappingService);
	}

	public void register() {
		BundleContext context = serviceReference.getBundle().getBundleContext();
		String[] classNames = getClassNames(serviceReference);

		root.register(context, classNames);
	}

	public void unregister() {
		root.unregister();
	}

	public void updateRegistrations() {
		// TODO Auto-generated method stub

	}

	private static Hashtable<String, Object> getProperties(
			ServiceReference<?> serviceReference) {
		Hashtable<String, Object> properties = new Hashtable<>();
		for (String propertyKey : serviceReference.getPropertyKeys()) {
			properties.put(propertyKey, serviceReference.getProperty(propertyKey));
		}
		properties.put(ServiceWrapperManagerImpl.class.getName(), true);

		return properties;
	}

	private static Set<Class<?>> getClasses(ServiceReference<?> serviceReference) {
		Set<Class<?>> serviceClasses = new HashSet<>();
		try {
			for (String className : getClassNames(serviceReference)) {
				serviceClasses.add(serviceReference.getBundle().loadClass(className));
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		return serviceClasses;
	}

	private static String[] getClassNames(ServiceReference<?> serviceReference) {
		return (String[]) serviceReference.getProperty("objectClass");
	}
}
