/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2.test.
 *
 * uk.co.strangeskies.p2.test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2.test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2.test.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2.test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySortedSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import aQute.bnd.version.Version;
import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.p2.P2RepositoryFactory;

public class P2RepositoryTest {
	private static final int SERVICE_TIMEOUT_MILLISECONDS = 2000;

	private static final String BUNDLE_EXISTS = "org.eclipse.osgi";
	private static final String BUNDLE_DOES_NOT_EXIST = "this.is.a.nonsense.bundle.id";

	private static final String FIRST = "first";
	private static final String SECOND = "second";

	private static final String INDIGO_REPOSITORY_LOCATION = "http://download.eclipse.org/releases/indigo/";
	private static P2Repository INDIGO_REPOSITORY;
	private static Exception INDIGO_REPOSITORY_EXCEPTION;

	@BeforeClass
	public static void createRepositories() {
		try {
			INDIGO_REPOSITORY = createRepository(FIRST, INDIGO_REPOSITORY_LOCATION);
		} catch (Exception e) {
			INDIGO_REPOSITORY_EXCEPTION = e;
		}
	}

	public static P2Repository getIndigoRepository() throws Exception {
		if (INDIGO_REPOSITORY != null) {
			return INDIGO_REPOSITORY;
		} else {
			throw INDIGO_REPOSITORY_EXCEPTION;
		}
	}

	protected static <T> T getService(Class<T> clazz) {
		try {
			BundleContext context = FrameworkUtil.getBundle(P2RepositoryTest.class).getBundleContext();

			ServiceTracker<T, T> st = new ServiceTracker<>(context, clazz, null);
			st.open();
			try {
				return st.waitForService(SERVICE_TIMEOUT_MILLISECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@Test
	public void createRepositoryTest() throws Exception {
		P2Repository test = createRepository(FIRST, INDIGO_REPOSITORY_LOCATION);

		assertEquals(FIRST, test.getName());
	}

	@Test
	public void findBundleTest() throws Exception {
		assertEquals(asList(BUNDLE_EXISTS), getIndigoRepository().list(BUNDLE_EXISTS));
	}

	@Test
	public void findBundleVersionsTest() throws Exception {
		Set<Version> versions = getIndigoRepository().versions(BUNDLE_EXISTS);

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println(versions);
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();

		assertTrue(versions.size() > 1);
		for (Version version : versions) {
			assertEquals(3, version.getMajor());
			assertEquals(7, version.getMinor());
		}
	}

	@Test
	public void findBundlesTest() throws Exception {
		List<String> bundles = getIndigoRepository().list("*" + BUNDLE_EXISTS + "*");

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println(bundles);
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();

		assertTrue(bundles.size() > 1);
		assertTrue(bundles.contains(BUNDLE_EXISTS));
	}

	@Test
	public void missingBundleTest() throws Exception {
		assertEquals(emptyList(), getIndigoRepository().list(BUNDLE_DOES_NOT_EXIST));
	}

	@Test
	public void missingBundleVersionsTest() throws Exception {
		assertEquals(emptySortedSet(), getIndigoRepository().versions(BUNDLE_DOES_NOT_EXIST));
	}

	@Test
	public void missingBundlesTest() throws Exception {
		assertEquals(emptyList(), getIndigoRepository().list("*" + BUNDLE_DOES_NOT_EXIST + "*"));
	}

	private static P2Repository createRepository(String name, String location) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		map.put("location", location);

		P2Repository repository = getService(P2RepositoryFactory.class).create((l, m) -> System.out.println(l + ": " + m));
		repository.setProperties(map);

		return repository;
	}
}
