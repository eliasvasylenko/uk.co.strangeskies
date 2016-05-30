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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static org.osgi.framework.Constants.FRAMEWORK_BUNDLE_PARENT;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.p2.P2RepositoryFactory;

@Component(service = P2BndRepositoryManager.class, scope = ServiceScope.BUNDLE)
public class P2BndRepositoryManager {
	private static final int FRAMEWORK_TIMEOUT_MILLISECONDS = 4000;
	private static final int SERVICE_TIMEOUT_MILLISECONDS = 4000;

	private static final String OSGI_CLEAN = "osgi.clean";
	private static final String OSGI_CLEAR_PERSISTED_STATE = "clearPersistedState";

	private static final List<String> FORWARD_VERSIONED_PACKAGES = unmodifiableList(asList(
			"uk.co.strangeskies.osgi;version=\"1.0.0\"",

			/*
			 * TODO There are just for in-framework logging?
			 */
			"uk.co.strangeskies.osgi.frameworkwrapper;version=\"1.0.0\"", "uk.co.strangeskies.bnd;version=\"1.0.0\"",
			"uk.co.strangeskies.osgi.frameworkwrapper.server;version=\"1.0.0\"",
			"uk.co.strangeskies.osgi.servicewrapper;version=\"1.0.0\"", "uk.co.strangeskies.utilities.text;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.classpath;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.collection;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.factory;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.flowcontrol;version=\"1.0.0\"",
			"uk.co.strangeskies.utilities.function;version=\"1.0.0\"", "uk.co.strangeskies.utilities.tuple;version=\"1.0.0\"",
			"uk.co.strangeskies.p2;version=\"1.0.0\"", "uk.co.strangeskies.p2.bnd;version=\"1.0.0\"",
			"org.osgi.service.repository;version=\"1.0.0\"", "org.osgi.resource;version=\"1.0.0\"",

			"uk.co.strangeskies.utilities;version=\"1.0.0\"", "uk.co.strangeskies.p2;version=\"1.0.0\"",
			"aQute.bnd.service;version=\"4.1.0\"", "aQute.bnd.service.repository;version=\"1.3.0\"",
			"aQute.bnd.version;version=\"1.3.0\"", "aQute.bnd.osgi;version=\"2.4.0\"",
			"aQute.service.reporter;version=\"1.0.0\""));

	private static final List<String> FORWARD_PACKAGES = unmodifiableList(
			FORWARD_VERSIONED_PACKAGES.stream().map(s -> s.split(";")[0]).collect(Collectors.toList()));

	@SuppressWarnings("serial")
	private static final Map<String, String> FRAMEWORK_PROPERTIES = new HashMap<String, String>() {
		{
			put(OSGI_CLEAN, Boolean.toString(true));
			put(OSGI_CLEAR_PERSISTED_STATE, Boolean.toString(true));

			put(FRAMEWORK_SYSTEMPACKAGES_EXTRA, FORWARD_VERSIONED_PACKAGES.stream().collect(joining(",")));
			put(FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
		}
	};

	private Set<P2BndRepository> frameworkUsers = new HashSet<>();
	private FrameworkWrapper framework;
	private P2RepositoryFactory repositoryFactory;

	public P2BndRepositoryManager(FrameworkWrapper sharedFramework) {
		setFramework(sharedFramework);
	}

	public P2BndRepositoryManager() {}

	@Reference
	public void setFramework(FrameworkWrapper framework) {
		this.framework = framework;

		framework.setTimeoutMilliseconds(FRAMEWORK_TIMEOUT_MILLISECONDS);

		framework.setLaunchProperties(FRAMEWORK_PROPERTIES);

		framework.onStart().addObserver(f -> {
			framework.withService(P2RepositoryFactory.class, p -> {
				repositoryFactory = p;
			}, SERVICE_TIMEOUT_MILLISECONDS);
		});

		framework.onStop().addObserver(f -> repositoryFactory = null);
	}

	public synchronized FrameworkWrapper getFramework() {
		return framework;
	}

	public P2RepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	public static boolean classDelegationFilter(Class<?> clazz) {
		List<String> packages = new ArrayList<>(P2BndRepositoryManager.FORWARD_PACKAGES);

		for (String forwardPackage : packages) {
			String packageName = clazz.getPackage().getName();
			if (packageName.startsWith(forwardPackage + ".") || packageName.equals(forwardPackage)) {
				return true;
			}
		}

		return false;
	}

	public P2BndRepository create() {
		return new P2BndRepository(this);
	}

	synchronized void open(P2BndRepository p2BndRepository) {
		frameworkUsers.add(p2BndRepository);
	}

	synchronized void close(P2BndRepository p2BndRepository) {
		if (frameworkUsers.remove(p2BndRepository) && frameworkUsers.isEmpty()) {
			getFramework().stopFramework();
		}
	}
}
