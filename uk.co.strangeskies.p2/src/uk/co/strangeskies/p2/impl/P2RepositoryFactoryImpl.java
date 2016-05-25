package uk.co.strangeskies.p2.impl;

import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.p2.P2RepositoryFactory;
import uk.co.strangeskies.utilities.Log;

@Component
public class P2RepositoryFactoryImpl implements P2RepositoryFactory {
	@Reference
	private IProvisioningAgentProvider agentProvider;
	private BundleContext bundleContext;

	@Override
	public P2Repository get(Log log) {
		return new P2RepositoryImpl(log, agentProvider, bundleContext);
	}

	@Activate
	public void activate(BundleContext context) {
		this.bundleContext = context;
	}
}
