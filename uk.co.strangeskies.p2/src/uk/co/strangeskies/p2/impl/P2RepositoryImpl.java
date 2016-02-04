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
package uk.co.strangeskies.p2.impl;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.repository.Repository;

import aQute.bnd.service.Plugin;
import aQute.bnd.service.RemoteRepositoryPlugin;
import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.version.Version;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;

/**
 * A wrapper for an Eclipse p2 repository implementing the
 * {@link RemoteRepositoryPlugin} and {@link Repository} interfaces.
 * 
 * @author Elias N Vasylenko
 */
@Component(service = { RemoteRepositoryPlugin.class, Repository.class }, immediate = true, name = "P2RepositoryImpl")
public class P2RepositoryImpl implements RemoteRepositoryPlugin, Repository, Plugin {
	private static final String MARS_UPDATE_SITE = "http://download.eclipse.org/releases/mars/";

	/**
	 * Property key for repository name.
	 */
	public static final String PROP_NAME = "name";

	/**
	 * Property key for repository location. Setting this property to a value is
	 * equivalent to setting both the {@link #PROP_METADATA_LOCATION} and
	 * {@link #PROP_ARTIFACT_LOCATION} to that value. This property should
	 * therefore not be used in conjunction with those properties.
	 */
	public static final String PROP_LOCATION = "location";

	/**
	 * Property for repository metadata location, to be used alongside the
	 * {@link #PROP_ARTIFACT_LOCATION} property. Typically, this may be the same
	 * as the artifact location, and so {@link #PROP_LOCATION} may be used
	 * instead.
	 */
	public static final String PROP_METADATA_LOCATION = "metadata";

	/**
	 * Property for repository artifact location, to be used alongside the
	 * {@link #PROP_METADATA_LOCATION} property. Typically, this may be the same
	 * as the metadata location, and so {@link #PROP_LOCATION} may be used
	 * instead.
	 */
	public static final String PROP_ARTIFACT_LOCATION = "artifact";

	/**
	 * Property for cache location, with a default given by
	 * {@link #DEFAULT_CACHE_DIR} in the user's home directory.
	 */
	public static final String PROP_CACHE_DIR = "cache";

	/**
	 * Default location for offline caching of repository artifacts.
	 */
	public static final String DEFAULT_CACHE_DIR = ".bnd" + File.separator + "cache" + File.separator + "p2";

	/**
	 * The length of time in seconds that cached artifact downloads will be
	 * retained and remain valid. Defaults to
	 * {@value #DEFAULT_CACHE_TIMEOUT_SECONDS} seconds.
	 */
	public static final String PROP_CACHE_TIMEOUT_SECONDS = "timeout";

	/**
	 * Default cache timeout in seconds.
	 */
	public static final int DEFAULT_CACHE_TIMEOUT_SECONDS = 30;

	private String name;
	private File cacheDir;
	private Log log = (l, s) -> {};
	private int cacheTimeoutSeconds;

	private URL metadataLocation;
	private URL artifactLocation;

	private boolean initialised = false;

	@Reference
	private IProvisioningAgentProvider agentProvider;

	private BundleContext bundleContext;

	/**
	 * Create a new unconfigured repository with sensible defaults where
	 * appropriate.
	 */
	public P2RepositoryImpl() {
		cacheDir = new File(System.getProperty("user.home") + File.separator + DEFAULT_CACHE_DIR);

		System.out.println("started");
	}

	@Activate
	public void activate(BundleContext context) {
		this.bundleContext = context;
	}

	private synchronized void initialise() {
		try {
			String localString = bundleContext.getBundle().getLocation();
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
				IProgressMonitor progressMonitor = new ProgressMonitorImpl();

				System.out.println("loading remote . . .");
				metadataManager.loadRepository(remote, progressMonitor);
				artifactManager.loadRepository(remote, progressMonitor);

				System.out.println("querying repository . . .");
				IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("id == $0", "org.eclipse.equinox.event");
				IQueryResult<IInstallableUnit> result = metadataManager.query(query, progressMonitor);
				System.out.println(result.toUnmodifiableSet());
			} catch (Exception pe) {
				throw new InvocationTargetException(pe);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private synchronized void ensureInitialised() {
		if (!initialised) {
			initialise();
			initialised = true;
		}
	}

	/*
	 * Plugin overrides
	 */

	@Override
	public void setProperties(Map<String, String> map) throws Exception {
		name = map.get(PROP_NAME);

		parseCacheProperties(map);
		parseLocationProperties(map);
	}

	protected void parseCacheProperties(Map<String, String> map) {
		cacheDir = null;
		if (map.containsKey(PROP_CACHE_DIR)) {
			cacheDir = new File(map.get(PROP_CACHE_DIR));
			if (!cacheDir.exists() || !cacheDir.isDirectory()) {
				log.log(Level.ERROR, "Bad cache directory setting: " + cacheDir);
			}

			if (map.containsKey(PROP_CACHE_TIMEOUT_SECONDS)) {
				try {
					cacheTimeoutSeconds = Integer.parseInt(map.get(PROP_CACHE_TIMEOUT_SECONDS));
				} catch (NumberFormatException e) {
					log.log(Level.ERROR, "Bad timeout setting: " + cacheTimeoutSeconds, e);
				}
			}
		}
	}

	protected void parseLocationProperties(Map<String, String> map) {
		String location = map.get(PROP_LOCATION);
		String metadataLocation = map.get(PROP_METADATA_LOCATION);
		String artifactLocation = map.get(PROP_ARTIFACT_LOCATION);

		if (location != null) {
			if (metadataLocation != null || artifactLocation != null) {
				log.log(Level.WARN, "Location setting is ambiguous with artifact or metadata settings");
			}
			metadataLocation = artifactLocation = location;
		}
		try {
			if (metadataLocation != null) {
				log.log(Level.ERROR, "Metadata location unspecified");
			} else {
				this.metadataLocation = new URL(metadataLocation);
			}
			if (artifactLocation == null) {
				log.log(Level.ERROR, "Artifact location unspecified");
			} else {
				this.artifactLocation = new URL(artifactLocation);
			}
		} catch (MalformedURLException e) {
			log.log(Level.ERROR, "Location URL is malformed", e);
		}
	}

	@Override
	public void setReporter(Reporter processor) {
		// TODO Auto-generated method stub
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	public void setLog(Log log) {
		this.log = log;
	}

	public void unsetLog(Log log) {
		this.log = log;
	}

	/*
	 * RepositoryPlugin overrides:
	 */

	@Override
	public PutResult put(InputStream stream, PutOptions options) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public File get(String bsn, Version version, Map<String, String> properties, DownloadListener... listeners)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canWrite() {
		return false;
	}

	@Override
	public List<String> list(String pattern) throws Exception {
		return Arrays.asList("test1", "test2");
	}

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception {
		SortedSet<Version> versions = new TreeSet<>();
		versions.add(new Version(1, 2, 3));
		return versions;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLocation() {
		return "{metadata: " + metadataLocation + ", artifact: " + artifactLocation + "}";
	}

	/*
	 * RemoteRepositoryPlugin overrides:
	 */

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getCacheDirectory() {
		return cacheDir;
	}

	/*
	 * Repository overrides:
	 */

	@Override
	public Map<Requirement, Collection<Capability>> findProviders(Collection<? extends Requirement> requirements) {
		ensureInitialised();

		Map<Requirement, Collection<Capability>> result = new HashMap<Requirement, Collection<Capability>>();
		for (Requirement requirement : requirements) {
			List<Capability> matches = new LinkedList<Capability>();
			result.put(requirement, matches);

			// capabilityIndex.appendMatchingCapabilities(requirement, matches);
		}
		return result;
	}
}
