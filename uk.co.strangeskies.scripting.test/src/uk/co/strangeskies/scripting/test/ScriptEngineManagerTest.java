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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import aQute.bnd.annotation.headers.RequireCapability;
import uk.co.strangeskies.scripting.RequireKotlin;
import uk.co.strangeskies.scripting.RequireLua;
import uk.co.strangeskies.scripting.RequirePython;
import uk.co.strangeskies.scripting.RequireRuby;
import uk.co.strangeskies.scripting.RequireScala;

@RequirePython
@RequireRuby
@RequireLua
@RequireKotlin
@RequireScala
@RequireCapability(ns = "osgi.service", filter = "(" + Constants.OBJECTCLASS + "=javax.script.ScriptEngineManager)")
public class ScriptEngineManagerTest {
	private static final int SERVICE_TIMEOUT_MILLISECONDS = 1000;
	private static final int TEST_TIMEOUT_MILLISECONDS = 5000;

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
	public void loadScalaEngineTest() {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		assertThat(manager.getEngineByName("scala"), notNullValue());
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void executeScalaScriptTest() throws ScriptException {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);
		ScriptEngine engine = manager.getEngineByName("scala");

		engine.eval("a = 1");
		engine.eval("b = 2");
		engine.eval("c = a + b");

		assertThat(engine.get("c"), equalTo(3));
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void loadLuaEngineTest() {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		assertThat(manager.getEngineByName("lua"), notNullValue());
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void executeLuaScriptTest() throws ScriptException {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);
		ScriptEngine engine = manager.getEngineByName("lua");

		engine.eval("a = 1");
		engine.eval("b = 2");
		engine.eval("c = a + b");

		assertThat(engine.get("c"), equalTo(3));
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void loadKotlinEngineTest() throws URISyntaxException {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		assertThat(manager.getEngineByName("kotlin"), notNullValue());
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void executeKotlinScriptTest() throws URISyntaxException, ScriptException {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);
		ScriptEngine engine = manager.getEngineByName("kotlin");

		engine.eval("val a = 1");
		engine.eval("val b = 2");

		assertThat(engine.eval("a + b"), equalTo(3l));
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void loadPythonEngineTest() {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		assertThat(manager.getEngineByName("python"), notNullValue());
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void executePythonScriptTest() throws ScriptException {
		ScriptEngineManager manager = getService(ScriptEngineManager.class);
		ScriptEngine engine = manager.getEngineByName("python");

		engine.eval("a = 1");
		engine.eval("b = 2");

		assertThat(engine.eval("a + b"), equalTo(3));
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void loadRubyEngineTest() {
		System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		assertThat(manager.getEngineByName("ruby"), notNullValue());
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void executeRubyScriptTest() throws ScriptException {
		System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
		ScriptEngineManager manager = getService(ScriptEngineManager.class);

		ScriptEngine engine = manager.getEngineByName("ruby");

		ScriptContext context = new SimpleScriptContext();
		context.setBindings(new SimpleBindings(), SimpleScriptContext.GLOBAL_SCOPE);
		engine.eval("a = 1", context);
		engine.eval("b = 2", context);

		assertThat(engine.eval("a + b"), equalTo(3l));
	}
}
