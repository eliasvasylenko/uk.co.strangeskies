package uk.co.strangeskies.gears.utilities.osgi;

import java.util.Map;

import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;

public interface ServiceWrapperManager extends EventListenerHook, FindHook {
	public void addServiceWrapper(ServiceWrapper<?> serviceWrapper,
			Map<String, Object> serviceProperties);

	public void modifyServiceWrapper(ServiceWrapper<?> serviceWrapper,
			Map<String, Object> serviceProperties);

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
	 * Implementations of {@link ServiceWrapperManagerRetroactingImpl} may choose
	 * to not accept or to ignore wrappers with this property set to true, as it
	 * may be necessary to incur a slight overhead over the entire
	 * service-framework in order to support this feature (all services may need
	 * to be proxied preemptively). It should also be noted that an implementation
	 * of {@link ServiceWrapperManagerRetroactingImpl} may only be able to wrap
	 * services which were registered after the manager was registered or created
	 * itself.
	 * </p>
	 */
	public static String SUPPORTS_WRAP_EXISTING_SERVICES = "supports.wrap.existing.services";
}
