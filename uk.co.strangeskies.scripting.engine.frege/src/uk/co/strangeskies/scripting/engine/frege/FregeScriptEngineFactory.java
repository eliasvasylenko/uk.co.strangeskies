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
 * This file is part of uk.co.strangeskies.scripting.engine.frege.
 *
 * uk.co.strangeskies.scripting.engine.frege is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.scripting.engine.frege is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.scripting.engine.frege;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import frege.runtime.Applicable;
import uk.co.strangeskies.scripting.InvocableScriptEngineDecorator;

public class FregeScriptEngineFactory extends frege.scriptengine.FregeScriptEngine.FregeScriptEngineFactory {
	@Override
	public ScriptEngine getScriptEngine() {
		ScriptEngine engine = super.getScriptEngine();

		return new InvocableScriptEngineDecorator() {
			@Override
			public ScriptEngine getComponent() {
				return engine;
			}

			@Override
			public Object invokeMethod(Object thiz, String name, Object... args)
					throws ScriptException, NoSuchMethodException {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException();
			}

			@Override
			public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
				Applicable function = (Applicable) engine.get(name);
				
				for (Object arg : args) {
					function.apply(arg);
				}

				return function.result().call();
			}
		};
	}
}
