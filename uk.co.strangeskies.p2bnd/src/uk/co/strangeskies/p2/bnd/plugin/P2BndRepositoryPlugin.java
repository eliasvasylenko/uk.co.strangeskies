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
package uk.co.strangeskies.p2.bnd.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.osgi.service.repository.Repository;

import aQute.bnd.service.RemoteRepositoryPlugin;
import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapperFactory;
import uk.co.strangeskies.osgi.frameworkwrapper.server.FrameworkWrapperServer;
import uk.co.strangeskies.p2.bnd.P2BndRepository;
import uk.co.strangeskies.p2.bnd.P2BndRepositoryManager;
import uk.co.strangeskies.utilities.Log;

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
public class P2BndRepositoryPlugin extends P2BndRepository {
	private static P2BndRepositoryManager SHARED_MANAGER;

	private static P2BndRepositoryManager getManager() {
		if (SHARED_MANAGER == null) {
			System.out.println("Fetching framework wrapper service loader");
			ServiceLoader<FrameworkWrapperFactory> serviceLoader = ServiceLoader.load(FrameworkWrapperFactory.class,
					P2BndRepositoryPlugin.class.getClassLoader());

			System.out.println("Loading framework wrapper service");
			FrameworkWrapperFactory frameworkWrapperFactory = StreamSupport.stream(serviceLoader.spliterator(), false)
					.findAny().orElseThrow(
							() -> new RuntimeException("Cannot find service implementing " + FrameworkWrapperServer.class.getName()));

			FrameworkWrapper frameworkWrapper = frameworkWrapperFactory.getFrameworkWrapper(P2BndRepositoryPlugin.class);

			frameworkWrapper.setLog((l, s) -> System.out.println(l + ": " + s), true);

			SHARED_MANAGER = new P2BndRepositoryManager(frameworkWrapper);
		}

		return SHARED_MANAGER;
	}

	protected P2BndRepositoryPlugin() {
		super(getManager());
	}

	public static void main(String... args) throws Exception {
		P2BndRepository first = test("hi");

		System.out.println(first.getName());

		first.close();
	}

	private static P2BndRepository test(String name) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		map.put("location", "http://download.eclipse.org/releases/mars/");

		P2BndRepository repo = new P2BndRepositoryPlugin();
		repo.setLog(new Log() {
			@Override
			public void log(Level level, String message) {
				System.out.println(level + ": " + message);
			}

			@Override
			public void log(Level level, String message, Throwable exception) {
				Log.super.log(level, message, exception);
				exception.printStackTrace();
			}
		});

		repo.setProperties(map);

		return repo;
	}
}
