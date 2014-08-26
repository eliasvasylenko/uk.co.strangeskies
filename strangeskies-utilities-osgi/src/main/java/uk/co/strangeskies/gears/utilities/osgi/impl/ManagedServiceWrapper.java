package uk.co.strangeskies.gears.utilities.osgi.impl;

import java.util.Map;

import uk.co.strangeskies.gears.utilities.osgi.ServiceWrapper;
import uk.co.strangeskies.gears.utilities.osgi.ServiceWrapper.HideServices;

class ManagedServiceWrapper<T> {
	private final ServiceWrapper<T> serviceWrapper;

	private Integer serviceRanking;
	private HideServices hideServices;

	public ManagedServiceWrapper(ServiceWrapper<T> serviceWrapper) {
		this.serviceWrapper = serviceWrapper;
	}

	public Integer getServiceRanking() {
		return serviceRanking;
	}

	public HideServices getHideServices() {
		return hideServices;
	}

	public T wrapService(T service) {
		return serviceWrapper.wrapService(service);
	}

	public void unwrapService(T service) {
		serviceWrapper.unwrapService(service);
	}

	public boolean wrapServiceProperties(Map<String, Object> serviceProperties) {
		return serviceWrapper.wrapServiceProperties(serviceProperties);
	}

	public Class<T> getServiceClass() {
		return serviceWrapper.getServiceClass();
	}

	public void update(int serviceRanking, HideServices hideServices) {
		this.serviceRanking = serviceRanking;
		this.hideServices = hideServices;
	}
}
