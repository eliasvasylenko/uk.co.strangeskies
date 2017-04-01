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
package uk.co.strangeskies.scripting.test.frege;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Ignore;
import org.junit.Test;

import uk.co.strangeskies.scripting.test.ScriptEngineTestBase;

@SuppressWarnings("javadoc")
//@RequireFregeScriptEngine
@Ignore
public class FregeScriptEngineTest extends ScriptEngineTestBase {
	public FregeScriptEngineTest() {
		super("frege");
	}

	@Override
	public void executeREPL() throws URISyntaxException, ScriptException {
		ScriptEngine engine = getScriptEngine();

		engine.eval("a = 1");
		engine.eval("b = 2");

		assertThat(engine.eval("a + b"), equalTo(3));
	}

	@Test
	public void invokeFunction() throws ScriptException, NoSuchMethodException {
		ScriptEngine engine = getScriptEngine();

		engine.eval("longer :: String -> String -> Bool\nlonger s1 s2 = s1.length > s2.length");
		System.out.println(engine.eval("longer \"first\" \"second\""));
		System.out.println(engine.eval("longer \"second\" \"first\""));

		System.out.println(engine.getContext().getAttribute("longer"));

		Object longerResult = ((Invocable) engine).invokeFunction("longer", "first", "second");

		System.out.println(longerResult.getClass());
		System.out.println(longerResult);
	}
}
