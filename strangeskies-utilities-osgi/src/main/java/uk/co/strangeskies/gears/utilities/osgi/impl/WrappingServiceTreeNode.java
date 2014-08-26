package uk.co.strangeskies.gears.utilities.osgi.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import uk.co.strangeskies.gears.utilities.osgi.ServiceWrapper.HideServices;

public class WrappingServiceTreeNode {
	private final Object service;
	private final Hashtable<String, Object> properties;

	private final Set<WrappingServiceTreeNode> wrappingServices;

	private boolean visible;

	private ServiceRegistration<?> registration;

	public WrappingServiceTreeNode(Object service,
			Hashtable<String, Object> properties) {
		this.service = service;
		this.properties = properties;

		wrappingServices = new HashSet<>();

		visible = false;
	}

	public Object getService() {
		return service;
	}

	public Hashtable<String, Object> getProperties() {
		return properties;
	}

	public Set<WrappingServiceTreeNode> getWrappingServices() {
		return wrappingServices;
	}

	public boolean isVisible() {
		return visible;
	}

	public WrappingServiceTreeNode wrap(ManagedServiceWrapper<?> serviceWrapper,
			Set<Class<?>> classes) {
		if (!classes.contains(serviceWrapper.getServiceClass()))
			throw new IllegalArgumentException();

		Hashtable<String, Object> wrappingProperties = getProperties();
		if (serviceWrapper.wrapServiceProperties(wrappingProperties)) {
			Object wrappingService = wrapService(getService(), serviceWrapper,
					classes);

			if (serviceWrapper.getHideServices() == HideServices.WHEN_WRAPPED
					|| serviceWrapper.getHideServices() == HideServices.SILENTLY)
				visible = false;

			WrappingServiceTreeNode compoundWrappedService = new WrappingServiceTreeNode(
					wrappingService, wrappingProperties);
			wrappingServices.add(compoundWrappedService);
			return compoundWrappedService;
		}

		if (serviceWrapper.getHideServices() == HideServices.ALWAYS)
			visible = false;

		return null;
	}

	public <T> Object wrapService(final Object service,
			ManagedServiceWrapper<T> serviceWrapper, Set<Class<?>> classes) {
		@SuppressWarnings("unchecked")
		final T wrappingService = serviceWrapper.wrapService((T) service);
		final Class<T> wrapperClass = serviceWrapper.getServiceClass();

		List<Class<?>> orderedServiceClasses = new ArrayList<>(classes);
		orderedServiceClasses.remove(wrapperClass);
		orderedServiceClasses.add(0, wrapperClass);

		return Proxy.newProxyInstance(null,
				orderedServiceClasses.toArray(new Class<?>[0]),
				new InvocationHandler() {
					@Override
					public Object invoke(Object object, Method method, Object[] args)
							throws Throwable {
						Class<?> declaringClass = method.getDeclaringClass();
						/*
						 * TODO this ^ isn't good enough. Need to be able to change
						 * preference to 'wrapperClass' when more than one interface
						 * contains a method.
						 * 
						 * WILL REPLACE WITH commons-proxy
						 */

						if (declaringClass == Object.class
								|| declaringClass == wrapperClass) {
							return method.invoke(wrappingService, args);
						} else {
							return method.invoke(service, args);
						}
					}
				});
	}

	public void register(BundleContext context, String[] classNames) {
		if (isVisible())
			registration = context.registerService(classNames, getService(),
					getProperties());

		for (WrappingServiceTreeNode wrappingService : getWrappingServices())
			wrappingService.register(context, classNames);
	}

	public void unregister() {
		if (registration != null) {
			registration.unregister();
			registration = null;
		}

		for (WrappingServiceTreeNode wrappingService : getWrappingServices())
			wrappingService.unregister();
	}
}
