package uk.co.strangeskies.osgi.utilities;

import java.rmi.registry.Registry;

import uk.co.strangeskies.utilities.Log;

public class ExtensionManager implements BundleListener {
	private BundleContext context;
	private BundleCapability capability;
	private final Registry registry = new Registry();

	@Activate
	protected void activate(ComponentContext cc) {
		this.context = cc.getBundleContext();
		context.addBundleListener(this);

		capability = context.getBundle().adapt(BundleWiring.class)
				.getCapabilities("osgi.extender").get(0);

		for (Bundle bundle : context.getBundles()) {
			if ((bundle.getState() & (Bundle.STARTING | Bundle.ACTIVE)) != 0
					&& isRegisterable(bundle)) {
				registry.register(bundle);
			}
		}
	}

	@Deactivate
	protected void deactivate(ComponentContext context) throws Exception {
		this.context.removeBundleListener(this);
		registry.close();
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		if (isRegisterable(event.getBundle())) {
			switch (event.getType()) {
			case BundleEvent.STARTED:
				registry.register(event.getBundle());
				break;

			case BundleEvent.STOPPED:
				registry.unregister(event.getBundle());
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
		registry.setLog(log);
	}
}
