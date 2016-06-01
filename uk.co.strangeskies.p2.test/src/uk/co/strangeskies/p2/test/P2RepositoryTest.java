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
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.strangeskies.p2.P2Repository;
import uk.co.strangeskies.p2.P2RepositoryFactory;

public class P2RepositoryTest {
	private static final int SERVICE_TIMEOUT_MILLISECONDS = 2000;

	private static final String BUNDLE_EXISTS = "org.eclipse.osgi";
	private static final String BUNDLE_DOES_NOT_EXIST = "this.is.a.nonsense.bundle.id";

	private static final String FIRST = "first";
	private static final String SECOND = "second";

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
		P2Repository test = createRepository(FIRST);

		assertEquals(FIRST, test.getName());
	}

	@Test
	public void findExactBundleTest() throws Exception {
		P2Repository test = createRepository(FIRST);

		assertEquals(asList(BUNDLE_EXISTS), test.list(BUNDLE_EXISTS));
	}

	@Test
	public void missingBundleTest() throws Exception {
		P2Repository test = createRepository(FIRST);

		assertEquals(emptyList(), test.list(BUNDLE_DOES_NOT_EXIST));
	}

	@Test
	public void missingBundlesTest() throws Exception {
		P2Repository test = createRepository(FIRST);

		assertEquals(emptyList(), test.list("*" + BUNDLE_DOES_NOT_EXIST + "*"));
	}

	private P2Repository createRepository(String name) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		map.put("location", "http://download.eclipse.org/releases/mars/");

		P2Repository repository = getService(P2RepositoryFactory.class).create((l, m) -> System.out.println(l + ": " + m));
		repository.setProperties(map);

		return repository;
	}
}
