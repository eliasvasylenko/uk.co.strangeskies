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

import static java.lang.Boolean.TRUE;
import static uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper.OSGI_CLEAN;
import static uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper.OSGI_CLEAR_PERSISTED_STATE;
import static uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper.OSGI_CONSOLE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.p2.P2RepositoryFactory;

@Component(service = P2BndRepositoryManager.class, scope = ServiceScope.BUNDLE)
public class P2BndRepositoryManager {
	private static final int FRAMEWORK_TIMEOUT_MILLISECONDS = 60000;
	private static final int SERVICE_TIMEOUT_MILLISECONDS = 4000;

	@SuppressWarnings("serial")
	private static final Map<String, String> FRAMEWORK_PROPERTIES = new HashMap<String, String>() {
		{
			put(OSGI_CLEAN, TRUE.toString());
			put(OSGI_CLEAR_PERSISTED_STATE, TRUE.toString());
			put(OSGI_CONSOLE, "");
			put("osgi.noShutdown", "true");
			put("eclipse.ignoreApp", "true");
			put("osgi.bundles.defaultStartLevel", "4");
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
