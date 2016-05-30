/*
] * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.p2.bnd;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.service.repository.Repository;

import aQute.bnd.service.Actionable;
import aQute.bnd.service.Plugin;
import aQute.bnd.service.Refreshable;
import aQute.bnd.service.Registry;
import aQute.bnd.service.RegistryPlugin;
import aQute.bnd.service.RemoteRepositoryPlugin;
import aQute.bnd.service.ResourceHandle;
import aQute.bnd.service.Strategy;
import aQute.bnd.service.repository.InfoRepository;
import aQute.bnd.service.repository.SearchableRepository;
import aQute.bnd.version.Version;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.bnd.ReporterLog;
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.function.ThrowingFunction;

/**
 * This class is not primarily intended to be used within OSGi environments. For
 * an OSGi enabled implementation of {@link RemoteRepositoryPlugin} and
 * {@link Repository} which provides p2 repository support, the
 * {@code uk.co.strangeskies.p2.P2RepositoryImpl} class in the
 * {@code uk.co.strangeskies.p2} project should be used instead. This class is
 * simply a wrapper for that implementation for use in non OSGi environments,
 * and creates a framework internally to host the necessary Eclipse Project
 * bundles.
 * <p>
 *
 * @author Elias N Vasylenko
 */
public class P2BndRepository implements RemoteRepositoryPlugin, Repository, Plugin, Closeable, Refreshable, Actionable,
		RegistryPlugin, SearchableRepository, InfoRepository {
	private final P2BndRepositoryManager manager;
	private boolean closed;

	private Log log = (l, s) -> {
		System.out.println(l + ": " + s);
	};

	private Map<String, String> properties;
	private Reporter reporter;
	private P2Repository repository;

	protected P2BndRepository(P2BndRepositoryManager manager) {
		this.manager = manager;
		log.log(Level.INFO, "Creating P2BndRepository");
		manager.open(this);
	}

	@Override
	public void close() throws IOException {
		manager.close(this);
		closed = true;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public Log getLog() {
		WeakReference<P2BndRepository> logSource = new WeakReference<>(this);

		return new Log() {
			@Override
			public void log(Level level, String message) {
				P2BndRepository repository = logSource.get();
				if (repository != null) {
					repository.log.log(level, message);
				}
			}

			@Override
			public void log(Level level, String message, Throwable exception) {
				P2BndRepository repository = logSource.get();
				if (repository != null) {
					repository.log.log(level, message, exception);
				}
			}
		};
	}

	@Override
	public void setProperties(Map<String, String> properties) throws Exception {
		log.log(Level.WARN, "Setting P2 repository properties: " + properties);

		this.properties = properties;
	}

	@Override
	public void setReporter(Reporter processor) {
		log.log(Level.WARN, "Setting P2 repository reporter");

		reporter = processor;

		log = new ReporterLog(reporter);
	}

	@Override
	public synchronized PutResult put(InputStream stream, PutOptions options) throws Exception {
		return withConnection(repository -> repository.put(stream, options));
	}

	@Override
	public synchronized File get(String bsn, Version version, Map<String, String> properties,
			DownloadListener... listeners) throws Exception {
		return withConnection(repository -> repository.get(bsn, version, properties, listeners));
	}

	private <T, E extends Exception> T withConnection(ThrowingFunction<P2Repository, T, E> action) throws E {
		if (closed) {
			throw new IllegalStateException("Cannot use after close");
		}

		return manager.getFramework().withFramework(() -> {
			if (repository == null) {
				repository = manager.getRepositoryFactory().get();
			}

			if (properties != null)
				repository.setProperties(properties);
			if (reporter != null)
				repository.setReporter(reporter);

			return action.apply(repository);
		});
	}

	@Override
	public boolean canWrite() {
		return withConnection(repository -> repository.canWrite());
	}

	@Override
	public List<String> list(String pattern) throws Exception {
		return withConnection(repository -> repository.list(pattern));
	}

	@Override
	public SortedSet<Version> versions(String bsn) throws Exception {
		return withConnection(repository -> repository.versions(bsn));
	}

	@Override
	public String getName() {
		return withConnection(repository -> repository.getName());
	}

	@Override
	public String getLocation() {
		return withConnection(repository -> repository.getLocation());
	}

	@Override
	public Map<Requirement, Collection<Capability>> findProviders(Collection<? extends Requirement> requirements) {
		return withConnection(repository -> repository.findProviders(requirements));
	}

	@Override
	public ResourceHandle getHandle(String bsn, String version, Strategy strategy, Map<String, String> properties)
			throws Exception {
		return withConnection(repository -> repository.getHandle(bsn, version, strategy, properties));
	}

	@Override
	public synchronized File getCacheDirectory() {
		return withConnection(repository -> repository.getCacheDirectory());
	}

	@Override
	public ResourceDescriptor getDescriptor(String bsn, Version version) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
	public void setRegistry(Registry registry) {
		// TODO Auto-generated method stub

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
	public boolean refresh() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public File getRoot() {
		// TODO Auto-generated method stub
		return null;
	}
}
