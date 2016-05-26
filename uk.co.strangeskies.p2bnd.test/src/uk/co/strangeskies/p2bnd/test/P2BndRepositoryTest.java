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
package uk.co.strangeskies.p2bnd.test;

import static uk.co.strangeskies.p2.bnd.P2BndRepository.FRAMEWORK_PROPERTIES;
import static uk.co.strangeskies.p2.bnd.P2BndRepository.SERVICE_TIMEOUT_MILLISECONDS;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import uk.co.strangeskies.p2.bnd.P2BndRepository;
import uk.co.strangeskies.p2.bnd.SharedFrameworkWrapper;

@SuppressWarnings("javadoc")
public class P2BndRepositoryTest {
	private static boolean INITIALISED = false;
	private static final String FIRST = "first";
	private static final String SECOND = "second";

	@Before
	public void configureSharedFramework() {
		if (!INITIALISED) {
			P2BndRepository
					.setSharedFramework(new SharedFrameworkWrapper(1000, SERVICE_TIMEOUT_MILLISECONDS, FRAMEWORK_PROPERTIES));
			INITIALISED = true;
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
		/*
		 * repo.setLog(new Log() {
		 * 
		 * @Override public void log(Level level, String message) {
		 * System.out.println(level + ": " + message); }
		 * 
		 * @Override public void log(Level level, String message, Throwable
		 * exception) { Log.super.log(level, message, exception);
		 * exception.printStackTrace(); } });
		 */
		repo.setProperties(map);

		return repo;
	}

}
