/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.scripting.test.
 *
 * uk.co.strangeskies.scripting.test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.scripting.test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.scripting.test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.ServiceTracker;

import aQute.bnd.annotation.headers.RequireCapability;
import uk.co.strangeskies.scripting.RequireFrege;
import uk.co.strangeskies.scripting.RequireKotlin;
import uk.co.strangeskies.scripting.RequirePython;
import uk.co.strangeskies.scripting.RequireRuby;

@RequirePython
@RequireFrege
@RequireRuby
@RequireKotlin
@RequireCapability(ns = "osgi.service", filter = "(" + Constants.OBJECTCLASS + "=javax.script.ScriptEngineManager)")
public class ScriptEngineManagerTest {
	private static final int SERVICE_TIMEOUT_MILLISECONDS = 1000;
	private static final int TEST_TIMEOUT_MILLISECONDS = 2000;

	private BundleContext getBundleContext() {
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	}

	private <T> T getService(Class<T> clazz) {
		try {
			BundleContext context = getBundleContext();

			ServiceTracker<T, T> serviceTracker = new ServiceTracker<>(context, clazz, null);
			serviceTracker.open();
			try {
				return serviceTracker.waitForService(SERVICE_TIMEOUT_MILLISECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void loadRunnableScriptTest() {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		assertThat(manager, notNullValue());
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void loadLuaEngineTest() {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		assertThat(manager.getEngineByName("lua"), notNullValue());
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void loadPythonEngineTest() {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		ScriptEngine engine;
		try {
			engine = (ScriptEngine) FrameworkUtil.getBundle(org.python.Version.class).getBundleContext().getBundle()
					.adapt(BundleWiring.class).getClassLoader().loadClass("PyScriptEngineFactory").newInstance();

			System.out.println(engine);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		assertThat(manager.getEngineByName("python"), notNullValue());
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void loadRubyEngineTest() {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		assertThat(manager.getEngineByName("ruby"), notNullValue());
	}
}
