/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2.
 *
 * uk.co.strangeskies.p2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

//@Component(immediate = true)
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
				throw new IllegalStateException(
						"Couldn't load metadata repository manager");
			}

			/*
			 * Load artifact manager
			 */
			IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) provisioningAgent
					.getService(IArtifactRepositoryManager.SERVICE_NAME);
			if (artifactManager == null) {
				throw new IllegalStateException(
						"Couldn't load artifact repository manager");
			}

			/*
			 * Load remote repository
			 */
			try {
				IProgressMonitor progressMonitor = new ProgressMonitorImpl();

				System.out.println("loading remote . . .");
				metadataManager.loadRepository(remote, progressMonitor);
				artifactManager.loadRepository(remote, progressMonitor);

				System.out.println("querying repository . . .");
				IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("id == $0",
						"org.eclipse.equinox.event");
				IQueryResult<IInstallableUnit> result = metadataManager.query(query,
						progressMonitor);
				System.out.println(result.toUnmodifiableSet());
			} catch (Exception pe) {
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

class ProgressMonitorImpl implements IProgressMonitor {
	private boolean cancelled = false;

	@Override
	public void beginTask(String name, int totalWork) {}

	@Override
	public void done() {}

	@Override
	public void internalWorked(double work) {}

	@Override
	public boolean isCanceled() {
		return cancelled;
	}

	@Override
	public void setCanceled(boolean value) {
		cancelled = value;
	}

	@Override
	public void setTaskName(String name) {}

	@Override
	public void subTask(String name) {
		System.out.println("  " + name);
	}

	@Override
	public void worked(int work) {}
}
