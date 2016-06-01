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

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.expression.SimplePattern;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.BundleContext;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.Repository;

import aQute.bnd.osgi.Jar;
import aQute.bnd.service.Registry;
import aQute.bnd.service.RemoteRepositoryPlugin;
import aQute.bnd.service.RepositoryListenerPlugin;
import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.version.Version;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.bnd.ReporterLog;
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.utilities.Log;

/**
 * A wrapper for an Eclipse p2 repository implementing the
 * {@link RemoteRepositoryPlugin} and {@link Repository} interfaces.
 * 
 * @author Elias N Vasylenko
 */
public class P2RepositoryImpl implements P2Repository, Log {
	private static final String MARS_UPDATE_SITE = "http://download.eclipse.org/releases/mars/";

	private String name;
	private File cacheDir;
	private int cacheTimeoutSeconds;

	private URL metadataLocation;
	private URL artifactLocation;

	private Reporter reporter;
	private Registry registry;
	private Log log;
	private final IProgressMonitor progressMonitor;

	private BundleContext bundleContext;

	private boolean initialised;
	private final IProvisioningAgentProvider agentProvider;
	private IMetadataRepositoryManager metadataManager;
	private IArtifactRepositoryManager artifactManager;

	@SuppressWarnings("javadoc")
	public P2RepositoryImpl(IProvisioningAgentProvider agentProvider, BundleContext bundleContext, Log log) {
		this.log = log;

		cacheDir = defaultCacheDirectory();

		this.agentProvider = agentProvider;
		this.bundleContext = bundleContext;

		progressMonitor = new ProgressMonitorImpl(this::getLog);
	}

	private static File defaultCacheDirectory() {
		return new File(System.getProperty("user.home") + File.separator + DEFAULT_CACHE_DIRECTORY);
	}

	public Log getLog() {
		return log != null ? log : (l, m) -> {};
	}

	protected synchronized void initialise() {
		if (!initialised) {
			try {
				getLog().log(Level.INFO, cacheDir.toString());
				URI local = cacheDir.toURI();
				URI remote = new URI(MARS_UPDATE_SITE);

				IProvisioningAgent provisioningAgent = agentProvider.createAgent(local);

				/*
				 * Load repository manager
				 */
				metadataManager = (IMetadataRepositoryManager) provisioningAgent
						.getService(IMetadataRepositoryManager.SERVICE_NAME);
				if (metadataManager == null) {
					throw new IllegalStateException("Couldn't load metadata repository manager");
				}

				/*
				 * Load artifact manager
				 */
				artifactManager = (IArtifactRepositoryManager) provisioningAgent
						.getService(IArtifactRepositoryManager.SERVICE_NAME);
				if (artifactManager == null) {
					throw new IllegalStateException("Couldn't load artifact repository manager");
				}

				/*
				 * Load remote repository
				 */
				try {
					getLog().log(Level.INFO, "loading remote . . .");
					metadataManager.loadRepository(remote, progressMonitor);
					artifactManager.loadRepository(remote, progressMonitor);
				} catch (Exception pe) {
					throw new InvocationTargetException(pe);
				}

				initialised = true;
			} catch (Exception e) {
				getLog().log(Level.ERROR, e);
				throw new RuntimeException(e);
			}
		}
	}

	/*
	 * Plugin overrides
	 */

	@Override
	public void setProperties(Map<String, String> map) {
		name = map.get(NAME_PROPERTY);

		parseCacheProperties(map);
		parseLocationProperties(map);

		initialised = false;
	}

	protected void parseCacheProperties(Map<String, String> map) {
		if (map.containsKey(CACHE_DIRECTORY_PROPERTY)) {
			cacheDir = new File(map.get(CACHE_DIRECTORY_PROPERTY));
		} else {
			cacheDir = defaultCacheDirectory();
		}

		if (!cacheDir.exists() || !cacheDir.isDirectory()) {
			log.log(Level.ERROR, "Bad cache directory setting: " + cacheDir);
		}

		if (map.containsKey(CACHE_TIMEOUT_SECONDS_PROPERTY)) {
			try {
				cacheTimeoutSeconds = Integer.parseInt(map.get(CACHE_TIMEOUT_SECONDS_PROPERTY));
			} catch (NumberFormatException e) {
				log.log(Level.ERROR, "Bad timeout setting: " + cacheTimeoutSeconds, e);
			}
		} else {
			cacheTimeoutSeconds = DEFAULT_CACHE_TIMEOUT_SECONDS;
		}
	}

	protected void parseLocationProperties(Map<String, String> map) {
		String location = map.get(LOCATION_PROPERTY);
		String metadataLocation = map.get(METADATA_LOCATION_PROPERTY);
		String artifactLocation = map.get(ARTIFACT_LOCATION_PROPERTY);

		if (location != null) {
			if (metadataLocation != null || artifactLocation != null) {
				log.log(Level.WARN, "Location setting is ambiguous with artifact or metadata settings");
			}
			metadataLocation = artifactLocation = location;
		}
		try {
			if (metadataLocation == null) {
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
		log = new ReporterLog(reporter);
	}

	/*
	 * RepositoryPlugin overrides:
	 */

	@Override
	public PutResult put(InputStream stream, PutOptions options) {
		throw new UnsupportedOperationException();
	}

	@Override
	public File get(String bsn, Version version, Map<String, String> properties, DownloadListener... listeners)
			throws Exception {
		initialise();

		ResourceHandle handle = getHandle(bsn, version.toString(), Strategy.EXACT, properties);

		File file = (handle == null) ? null : handle.request();

		if (file != null) {
			for (DownloadListener listener : listeners) {
				try {
					listener.success(file);
				} catch (Exception e) {
					log(Level.ERROR, "Download listener failed for " + file, e);
				}
			}
		}

		return file;
	}

	@Override
	public boolean canWrite() {
		return false;
	}

	@Override
	public List<String> list(String pattern) {
		initialise();

		getLog().log(Level.INFO, "querying repository for bundles . . .");
		IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("id ~= $0", SimplePattern.compile(pattern));
		IQueryResult<IInstallableUnit> result = metadataManager.query(query, progressMonitor);

		return stream(result.spliterator(), false).map(i -> i.getId()).distinct().collect(toList());
	}

	@Override
	public SortedSet<Version> versions(String bsn) {
		initialise();

		getLog().log(Level.INFO, "querying repository for versions . . .");
		IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("id == $0", bsn);
		IQueryResult<IInstallableUnit> result = metadataManager.query(query, progressMonitor);

		return new TreeSet<>(
				stream(result.spliterator(), false).map(i -> new Version(i.getVersion().toString())).collect(toList()));
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
		initialise();

		if (bsn == null) {
			throw new IllegalArgumentException("Cannot resolve bundle: bundle symbolic name not specified.");
		}

		if (version == null) {
			version = properties.get(VERSION_KEY_PROPERTY);
		}

		return resolveBundle(bsn, version, strategy, properties);
	}

	private ResourceHandle resolveBundle(String bsn, String version, Strategy strategy, Map<String, String> properties) {
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
		Map<Requirement, Collection<Capability>> result = new HashMap<>();
		for (Requirement requirement : requirements) {
			List<Capability> matches = new LinkedList<>();
			result.put(requirement, matches);

			// capabilityIndex.appendMatchingCapabilities(requirement, matches);
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean refresh() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public File getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Runnable> actions(Object... target) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String tooltip(Object... target) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String title(Object... target) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	private void fireBundleAdded(File file) {
		if (registry == null)
			return;
		List<RepositoryListenerPlugin> listeners = registry.getPlugins(RepositoryListenerPlugin.class);
		Jar jar = null;
		for (RepositoryListenerPlugin listener : listeners) {
			try {
				if (jar == null)
					jar = new Jar(file);
				listener.bundleAdded(this, jar, file);
			} catch (Exception e) {
				if (reporter != null)
					reporter.warning("Repository listener threw an unexpected exception: %s", e);
			} finally {
				if (jar != null)
					jar.close();
			}
		}
	}

	@Override
	public Set<ResourceDescriptor> getResources(URI url, boolean includeDependencies) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ResourceDescriptor> query(String query) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addResource(ResourceDescriptor resource) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<ResourceDescriptor> findResources(Requirement requirement, boolean includeDependencies) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI browse(String searchString) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceDescriptor getDescriptor(String bsn, Version version) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(Level level, String message) {
		if (log != null) {
			log.log(level, message);
		}
	}

	@Override
	public void log(Level level, String message, Throwable exception) {
		if (log != null) {
			log.log(level, message, exception);
		}
	}
}
