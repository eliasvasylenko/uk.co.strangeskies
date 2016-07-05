package uk.co.strangeskies.osgi;

import static org.osgi.framework.Constants.SERVICE_RANKING;

import java.util.function.Consumer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class RankedServiceTracker<T> extends ServiceTracker<T, RankedService<T>> {
	public RankedServiceTracker(BundleContext context, Class<T> serviceClass, Consumer<RankedService<T>> add,
			Consumer<RankedService<T>> remove) {
		this(context, serviceClass, add, t -> {}, remove);
	}

	public RankedServiceTracker(BundleContext context, Class<T> serviceClass, Consumer<RankedService<T>> add,
			Consumer<RankedService<T>> modify, Consumer<RankedService<T>> remove) {
		super(context, serviceClass, new ServiceTrackerCustomizer<T, RankedService<T>>() {
			@Override
			public RankedService<T> addingService(ServiceReference<T> reference) {
				T service = reference.getBundle().getBundleContext().getService(reference);

				return new RankedService<T>(service, getRank(reference));
			}

			@Override
			public void modifiedService(ServiceReference<T> reference, RankedService<T> service) {
				int rank = getRank(reference);
			}

			@Override
			public void removedService(ServiceReference<T> reference, RankedService<T> service) {
				;
			}

			private int getRank(ServiceReference<?> reference) {
				Object property = reference.getProperty(SERVICE_RANKING);
				return (property instanceof Integer) ? ((Integer) property).intValue() : 0;
			}
		});
	}
}
