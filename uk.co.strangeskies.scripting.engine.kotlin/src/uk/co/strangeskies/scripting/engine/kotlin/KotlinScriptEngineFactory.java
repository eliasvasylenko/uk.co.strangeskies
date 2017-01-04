/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.scripting.engine.kotlin.
 *
 * uk.co.strangeskies.scripting.engine.kotlin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.scripting.engine.kotlin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.scripting.engine.kotlin;

import static com.intellij.openapi.util.Disposer.newDisposable;
import static java.util.Arrays.asList;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineFactoryBase;
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine;

import kotlin.script.templates.standard.ScriptTemplateWithBindings;
import uk.co.strangeskies.scripting.InvocableScriptEngineDecorator;

@SuppressWarnings("javadoc")
public class KotlinScriptEngineFactory extends KotlinJsr223JvmScriptEngineFactoryBase {
	private static final URL TEMPLATE_CLASS_PATH = KotlinScriptEngineFactory.class
			.getProtectionDomain()
			.getCodeSource()
			.getLocation();
	private static final String TEMPLATE_CLASS_NAME = ScriptTemplateWithBindings.class.getName();

	@Override
	public ScriptEngine getScriptEngine() {
		File templateClassPath;
		try {
			templateClassPath = new File(TEMPLATE_CLASS_PATH.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		ScriptEngine engine = new KotlinJsr223JvmLocalScriptEngine(newDisposable(), this, asList(templateClassPath),
				TEMPLATE_CLASS_NAME, ctx -> new Bindings[] { ctx.getBindings(ScriptContext.ENGINE_SCOPE) },
				new Class<?>[] { Map.class });

		return new InvocableScriptEngineDecorator() {
			@Override
			public Object invokeMethod(Object thiz, String name, Object... args)
					throws ScriptException, NoSuchMethodException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
				return null;
			}

			@Override
			public ScriptEngine getComponent() {
				return engine;
			}
		};
	}

	public static void main(String[] args) throws ScriptException {
		ScriptEngine engine = new KotlinScriptEngineFactory().getScriptEngine();

		engine.eval("fun test(arg: String) {println(\"Hello, World!\")}");
		engine.eval("test(\"hi\")");
	}
}
