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

import static org.osgi.framework.FrameworkUtil.getBundle;
import static uk.co.strangeskies.p2.bnd.P2BndRepository.FRAMEWORK_PROPERTIES;
import static uk.co.strangeskies.p2.bnd.P2BndRepository.SERVICE_TIMEOUT_MILLISECONDS;
import static uk.co.strangeskies.p2.bnd.P2BndRepository.setSharedFramework;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.strangeskies.p2.bnd.FrameworkWrapperFactory;
import uk.co.strangeskies.p2.bnd.P2BndRepository;
import uk.co.strangeskies.utilities.classpath.FilteringClassLoader;

@SuppressWarnings("javadoc")
public class P2BndRepositoryTest {
	private static boolean INITIALISED = false;
	private static final String FIRST = "first";
	private static final String SECOND = "second";

	@Before
	public void configureSharedFramework() {
		if (!INITIALISED) {
			ClassLoader classLoader = new FilteringClassLoader(
					getBundle(P2BndRepository.class).adapt(BundleWiring.class).getClassLoader(),
					P2BndRepository::classDelegationFilter);

			setSharedFramework(
					new FrameworkWrapperFactory(1000, SERVICE_TIMEOUT_MILLISECONDS, FRAMEWORK_PROPERTIES, classLoader));
			INITIALISED = true;
		}
	}

	protected <T> T getService(Class<T> clazz) {
		try {
			BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

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

		Thread.sleep(1500);

		first.getName();
	}

	@Test
	public void useAfterCloseTest() throws Exception {
		P2BndRepository first = test(FIRST);

		first.close();

		first.getName();
	}

	@Test
	public void getRepositoryNameTest() throws Exception {
		P2BndRepository first = test(FIRST);

		first.getName();

		first.close();
	}

	private P2BndRepository test(String name) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		map.put("location", "http://download.eclipse.org/releases/mars/");

		P2BndRepository repo = new P2BndRepository();
		repo.setProperties(map);

		return repo;
	}

}
