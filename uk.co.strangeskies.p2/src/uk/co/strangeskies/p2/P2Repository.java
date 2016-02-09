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

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.osgi.service.repository.Repository;

import aQute.bnd.service.Plugin;
import aQute.bnd.service.RemoteRepositoryPlugin;
import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.version.Version;

/**
 * A wrapper for an Eclipse p2 repository.
 * 
 * @author Elias N Vasylenko
 */
public interface P2Repository extends RemoteRepositoryPlugin, Repository, Plugin {
	/**
	 * Property key for repository name.
	 */
	public static final String NAME_PROPERTY = "name";

	/**
	 * Property key for repository location. Setting this property to a value is
	 * equivalent to setting both the {@link #METADATA_LOCATION_PROPERTY} and
	 * {@link #ARTIFACT_LOCATION_PROPERTY} to that value. This property should
	 * therefore not be used in conjunction with those properties.
	 */
	public static final String LOCATION_PROPERTY = "location";

	/**
	 * Property for repository metadata location, to be used alongside the
	 * {@link #ARTIFACT_LOCATION_PROPERTY} property. Typically, this may be the same
	 * as the artifact location, and so {@link #LOCATION_PROPERTY} may be used
	 * instead.
	 */
	public static final String METADATA_LOCATION_PROPERTY = "metadata";

	/**
	 * Property for repository artifact location, to be used alongside the
	 * {@link #METADATA_LOCATION_PROPERTY} property. Typically, this may be the same
	 * as the metadata location, and so {@link #LOCATION_PROPERTY} may be used
	 * instead.
	 */
	public static final String ARTIFACT_LOCATION_PROPERTY = "artifact";

	/**
	 * Property for cache location, with a default given by
	 * {@link #DEFAULT_CACHE_DIRECTORY} in the user's home directory.
	 */
	public static final String CACHE_DIRECTORY_PROPERTY = "cache";

	/**
	 * Default location for offline caching of repository artifacts.
	 */
	public static final String DEFAULT_CACHE_DIRECTORY = ".bnd" + File.separator + "cache" + File.separator + "p2";

	/**
	 * The length of time in seconds that cached artifact downloads will be
	 * retained and remain valid. Defaults to
	 * {@value #DEFAULT_CACHE_TIMEOUT_SECONDS} seconds.
	 */
	public static final String CACHE_TIMEOUT_SECONDS_PROPERTY = "timeout";

	/**
	 * Default cache timeout in seconds.
	 */
	public static final int DEFAULT_CACHE_TIMEOUT_SECONDS = 30;

	/*
	 * Plugin overrides
	 */

	@Override
	public void setProperties(Map<String, String> map) throws Exception;

	/*
	 * RepositoryPlugin overrides:
	 */

	@Override
	public PutResult put(InputStream stream, PutOptions options) throws Exception;

	@Override
	public File get(String bsn, Version version, Map<String, String> properties, DownloadListener... listeners) throws Exception;

	@Override
	public boolean canWrite();

	@Override
	public List<String> list(String pattern) throws Exception;

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception;

	@Override
	public String getName();

	@Override
	public String getLocation();

	/*
	 * RemoteRepositoryPlugin overrides:
	 */

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties)
			throws Exception;

	@Override
	public File getCacheDirectory();
}
