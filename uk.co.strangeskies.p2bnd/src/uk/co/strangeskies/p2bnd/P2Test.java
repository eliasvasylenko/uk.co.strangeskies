package uk.co.strangeskies.p2bnd;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class P2Test {
	private static final String MARS_UPDATE_SITE = "http://download.eclipse.org/releases/mars/";
	private static final String PROFILE_NAME = "test";

	@Reference
	private IProvisioningAgentProvider agentProvider;

	@Activate
	protected void activate(BundleContext context) throws Exception {
		System.out.println("Hey there . . .");

		try {
			String localString = context.getBundle().getLocation();
			localString = localString.substring(localString.indexOf(':') + 1);
			localString = localString.substring(0, localString.lastIndexOf('/'));
			localString = localString + "/p2/";
			System.out.println(localString);
			URI local = new URI(localString);
			URI remote = new URI(MARS_UPDATE_SITE);

			IProvisioningAgent provisioningAgent = agentProvider.createAgent(local);

			/*
			 * Load repository manager
			 */
			IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) provisioningAgent
					.getService(IMetadataRepositoryManager.SERVICE_NAME);
			if (metadataManager == null) {
				throw new IllegalStateException("Couldn't load metadata repository manager");
			}

			/*
			 * Load artifact manager
			 */
			IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) provisioningAgent
					.getService(IArtifactRepositoryManager.SERVICE_NAME);
			if (artifactManager == null) {
				throw new IllegalStateException("Couldn't load artifact repository manager");
			}

			/*
			 * Load remote repository
			 */
			try {
				metadataManager.loadRepository(remote, null);
				artifactManager.loadRepository(remote, null);

				IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("id == $0", "org.eclipse.equinox.event");
				IQueryResult<IInstallableUnit> result = metadataManager.query(query, null);
				System.out.println(result);
			} catch (ProvisionException pe) {
				throw new InvocationTargetException(pe);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			context.getBundle(0).stop();
		}
	}
}
