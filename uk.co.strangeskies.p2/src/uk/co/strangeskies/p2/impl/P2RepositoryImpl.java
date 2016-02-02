/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2bnd.
 *
 * uk.co.strangeskies.p2bnd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2bnd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2bnd.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2.impl;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.repository.Repository;

import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.version.Version;
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;

@Component(service = { P2Repository.class, Repository.class }, immediate = true)
public class P2RepositoryImpl implements P2Repository {
	private static final String DEFAULT_CACHE_DIR = ".bnd" + File.separator
			+ "cache" + File.separator + "p2";

	public static final String PROP_NAME = "name";
	public static final String PROP_LOCATION = "location";
	public static final String PROP_METADATA_LOCATION = "metadata";
	public static final String PROP_ARTIFACT_LOCATION = "artifact";
	public static final String PROP_CACHE = "cache";
	public static final String PROP_CACHE_TIMEOUT_SECONDS = "timeout";

	private String name;
	private File cacheDir;
	private Log log = (l, s) -> {};
	private int cacheTimeoutSeconds;

	private URL metadataLocation;
	private URL artifactLocation;

	private boolean initialised;

	public P2RepositoryImpl() {
		cacheDir = new File(
				System.getProperty("user.home") + File.separator + DEFAULT_CACHE_DIR);
	}

	private synchronized void initialise() {
		if (!initialised) {

			initialised = true;
		}
	}

	private synchronized void refresh() {
		initialised = false;
		initialise();
	}

	/*
	 * Plugin overrides
	 */

	public void setProperties(Map<String, String> map) throws Exception {
		name = map.get(PROP_NAME);

		parseCacheProperties(map);
		parseLocationProperties(map);
	}

	protected void parseCacheProperties(Map<String, String> map) {
		cacheDir = null;
		if (map.containsKey(PROP_CACHE)) {
			cacheDir = new File(map.get(PROP_CACHE));
			if (!cacheDir.exists() || !cacheDir.isDirectory()) {
				log.log(Level.ERROR, "Bad cache directory setting: " + cacheDir);
			}

			if (map.containsKey(PROP_CACHE_TIMEOUT_SECONDS)) {
				try {
					cacheTimeoutSeconds = Integer
							.parseInt(map.get(PROP_CACHE_TIMEOUT_SECONDS));
				} catch (NumberFormatException e) {
					log.log(Level.ERROR, "Bad timeout setting: " + cacheTimeoutSeconds,
							e);
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
				log.log(Level.WARN,
						"Location setting is ambiguous with artifact or metadata settings");
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
	public PutResult put(InputStream stream, PutOptions options)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public File get(String bsn, Version version, Map<String, String> properties,
			DownloadListener... listeners) throws Exception {
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
		return "{metadata: " + metadataLocation + ", artifact: " + artifactLocation
				+ "}";
	}

	/*
	 * RemoteRepositoryPlugin overrides:
	 */

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy,
			Map<String, String> properties) throws Exception {
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
	public Map<Requirement, Collection<Capability>> findProviders(
			Collection<? extends Requirement> requirements) {
		initialise();

		Map<Requirement, Collection<Capability>> result = new HashMap<Requirement, Collection<Capability>>();
		for (Requirement requirement : requirements) {
			List<Capability> matches = new LinkedList<Capability>();
			result.put(requirement, matches);

			// capabilityIndex.appendMatchingCapabilities(requirement, matches);
		}
		return result;
	}
}
