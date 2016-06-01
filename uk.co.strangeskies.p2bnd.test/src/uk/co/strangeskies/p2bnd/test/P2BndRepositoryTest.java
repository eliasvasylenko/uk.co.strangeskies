/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.p2bnd.test.
 *
 * uk.co.strangeskies.p2bnd.test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.p2bnd.test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.p2bnd.test.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.p2bnd.test;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.strangeskies.osgi.frameworkwrapper.FrameworkWrapper;
import uk.co.strangeskies.p2.bnd.P2BndRepository;
import uk.co.strangeskies.p2.bnd.P2BndRepositoryManager;

@SuppressWarnings("javadoc")
public class P2BndRepositoryTest {
	private static final int FRAMEWORK_TIMEOUT_MILLISECONDS = 1000;
	private static final int SERVICE_TIMEOUT_MILLISECONDS = 2000;

	private static P2BndRepositoryManager MANAGER;

	private static final String FIRST = "first";
	private static final String SECOND = "second";

	public P2BndRepositoryManager getManager() {
		if (MANAGER == null) {
			MANAGER = getService(P2BndRepositoryManager.class);
			FrameworkWrapper frameworkWrapper = MANAGER.getFramework();
			frameworkWrapper.setTimeoutMilliseconds(FRAMEWORK_TIMEOUT_MILLISECONDS);
		}

		return MANAGER;
	}

	protected static <T> T getService(Class<T> clazz) {
		try {
			BundleContext context = FrameworkUtil.getBundle(P2BndRepositoryTest.class).getBundleContext();

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
		P2BndRepository first = test(FIRST);

		first.close();
	}

	@Test
	public void createTwoRepositoriesTest() throws Exception {
		P2BndRepository first = test(FIRST);
		P2BndRepository second = test(SECOND);

		first.close();
		second.close();
	}

	@Test
	public void createRepositoryWaitReopenTest() throws Exception {
		P2BndRepository first = test(FIRST);
		P2BndRepository second = test(SECOND);

		second.close();

		sleep(FRAMEWORK_TIMEOUT_MILLISECONDS * 2);

		first.getName();

		first.close();
	}

	@Test(expected = IllegalStateException.class)
	public void useAfterCloseTest() throws Exception {
		P2BndRepository first = test(FIRST);

		first.close();

		first.getName();
	}

	@Test
	public void getRepositoryNameTest() throws Exception {
		P2BndRepository first = test(FIRST);

		assertEquals(FIRST, first.getName());

		first.close();
	}

	@Test
	public void getTwoRepositoryNamesTest() throws Exception {
		P2BndRepository first = test(FIRST);
		P2BndRepository second = test(SECOND);

		assertEquals(FIRST, first.getName());
		assertEquals(SECOND, second.getName());

		first.close();
		second.close();
	}

	private P2BndRepository test(String name) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		map.put("location", "http://download.eclipse.org/releases/mars/");

		P2BndRepository repo = getManager().create();
		repo.setProperties(map);

		return repo;
	}
}
