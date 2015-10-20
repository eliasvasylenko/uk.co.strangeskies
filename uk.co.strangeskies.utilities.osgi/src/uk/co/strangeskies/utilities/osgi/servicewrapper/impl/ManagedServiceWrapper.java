/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.osgi.
 *
 * uk.co.strangeskies.utilities.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.osgi.servicewrapper.impl;

import java.util.Map;

import uk.co.strangeskies.utilities.osgi.servicewrapper.ServiceWrapper;
import uk.co.strangeskies.utilities.osgi.servicewrapper.ServiceWrapper.HideServices;

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
