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
package uk.co.strangeskies.utilities.osgi;

import java.util.Map;

import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;

/**
 * Adding and removing service wrappers from this manager should see their
 * effects applied to all services within the OSGi service registry this manager
 * is registered with.
 * 
 * @author Elias N Vasylenko
 */
public interface ServiceWrapperManager extends EventListenerHook, FindHook {
	/**
	 * Add a wrapper to the manager. Existing services may be wrapped at this
	 * point according to the configuration of the wrapper.
	 * 
	 * @param serviceWrapper
	 * @param serviceProperties
	 */
	public void addServiceWrapper(ServiceWrapper<?> serviceWrapper,
			Map<String, Object> serviceProperties);

	/**
	 * Unregister a service wrapper from the manager. The effects of the new
	 * properties will be propagated immediately to any wrapping services as
	 * appropriate.
	 * 
	 * @param serviceWrapper
	 * @param serviceProperties
	 */
	public void modifyServiceWrapper(ServiceWrapper<?> serviceWrapper,
			Map<String, Object> serviceProperties);

	/**
	 * Unregister a service wrapper from the manager. This will have the side
	 * effect of removing all instances of wrapped services from the service
	 * registry, which could mean services in use are removed.
	 * 
	 * @param serviceWrapper
	 */
	public void removeServiceWrapper(ServiceWrapper<?> serviceWrapper);

	/**
	 * <p>
	 * If a property with this key is present on a {@link ServiceWrapperManager}
	 * service then the value should be of the type {@link Boolean}. This value
	 * then determines whether a wrapper be applied retroactively to services
	 * which already exist, and therefore may already be in use by other bundles.
	 * </p>
	 * 
	 * <p>
	 * Wrappers which maintain state should normally not set this value to
	 * {@link Boolean#TRUE}, as they won't generally have any way to determine the
	 * state they should adopt at the point at which they are added, since they
	 * may be added and removed multiple times, through multiple calls to
	 * {@link ServiceWrapper#wrapService(Object)}.
	 * </p>
	 * 
	 * <p>
	 * If this property is set to {@link Boolean#FALSE} then existing services
	 * will not be wrapped, so the wrapper can be sure that
	 * {@link ServiceWrapper#wrapService(Object)} is only ever called once for any
	 * service, at the point when that services is registered.
	 * </p>
	 * 
	 * <p>
	 * Implementations of {@link ServiceWrapperManager} may choose to not accept
	 * or to ignore wrappers with this property set to true, as it may be
	 * necessary to incur a slight overhead over the entire service-framework in
	 * order to support this feature (all services may need to be proxied
	 * preemptively). It should also be noted that an implementation of
	 * {@link ServiceWrapperManager} may only be able to wrap services which were
	 * registered after the manager was registered or created itself.
	 * </p>
	 */
	public static String SUPPORTS_WRAP_EXISTING_SERVICES = "supports.wrap.existing.services";
}
