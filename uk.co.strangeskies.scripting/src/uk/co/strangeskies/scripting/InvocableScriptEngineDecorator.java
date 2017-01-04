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
 * This file is part of uk.co.strangeskies.scripting.
 *
 * uk.co.strangeskies.scripting is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.scripting is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.scripting;

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

public interface InvocableScriptEngineDecorator extends ScriptEngine, InvocableBase {
	ScriptEngine getComponent();

	@Override
	default void setContext(ScriptContext context) {
		getComponent().setContext(context);
	}

	@Override
	default void setBindings(Bindings bindings, int scope) {
		getComponent().setBindings(bindings, scope);
	}

	@Override
	default void put(String key, Object value) {
		getComponent().put(key, value);
	}

	@Override
	default ScriptEngineFactory getFactory() {
		return getComponent().getFactory();
	}

	@Override
	default ScriptContext getContext() {
		return getComponent().getContext();
	}

	@Override
	default Bindings getBindings(int scope) {
		return getComponent().getBindings(scope);
	}

	@Override
	default Object get(String key) {
		return getComponent().get(key);
	}

	@Override
	default Object eval(Reader reader, Bindings n) throws ScriptException {
		return getComponent().eval(reader, n);
	}

	@Override
	default Object eval(String script, Bindings n) throws ScriptException {
		return getComponent().eval(script, n);
	}

	@Override
	default Object eval(Reader reader, ScriptContext context) throws ScriptException {
		return getComponent().eval(reader, context);
	}

	@Override
	default Object eval(String script, ScriptContext context) throws ScriptException {
		return getComponent().eval(script, context);
	}

	@Override
	default Object eval(Reader reader) throws ScriptException {
		return getComponent().eval(reader);
	}

	@Override
	default Object eval(String script) throws ScriptException {
		return getComponent().eval(script);
	}

	@Override
	default Bindings createBindings() {
		return getComponent().createBindings();
	}
}
