package uk.co.strangeskies.osgi.utilities;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import uk.co.strangeskies.utilities.Log;

public abstract class ExtensionManager implements BundleListener {
	private BundleContext context;
	private BundleCapability capability;
	private Log log;

	@Activate
	protected void activate(ComponentContext cc) {
		this.context = cc.getBundleContext();
		context.addBundleListener(this);

		capability = context.getBundle().adapt(BundleWiring.class)
				.getCapabilities("osgi.extender").get(0);

		for (Bundle bundle : context.getBundles()) {
			if ((bundle.getState() & (Bundle.STARTING | Bundle.ACTIVE)) != 0
					&& isRegisterable(bundle)) {
				register(bundle);
			}
		}
	}

	@Deactivate
	protected void deactivate(ComponentContext context) throws Exception {
		this.context.removeBundleListener(this);
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		if (isRegisterable(event.getBundle())) {
			switch (event.getType()) {
			case BundleEvent.STARTED:
				register(event.getBundle());
				break;

			case BundleEvent.STOPPED:
				unregister(event.getBundle());
				break;
			}
		}
	}

	private boolean isRegisterable(Bundle bundle) {
		boolean registerable = bundle.adapt(BundleWiring.class)
				.getRequirements("osgi.extender").stream()
				.anyMatch(r -> r.matches(capability));

		return registerable;
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	public void setLog(Log log) {
		this.log = log;
	}

	protected Log getLog() {
		return log;
	}

	protected abstract void register(Bundle bundle);

	protected abstract void unregister(Bundle bundle);
}
