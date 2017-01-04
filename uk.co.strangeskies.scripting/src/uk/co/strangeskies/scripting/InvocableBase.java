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

import static java.lang.reflect.Proxy.newProxyInstance;

import java.util.function.BiFunction;

import javax.script.Invocable;
import javax.script.ScriptException;

/**
 * A partial implementation of {@link Invocable} which implements
 * {@link #getInterface(Class)} and {@link #getInterface(Object, Class)} by
 * proxying the requested class and delegating invocations to
 * {@link #invokeFunction(String, Object...)} and
 * {@link #invokeMethod(Object, String, Object...)} respectively.
 * <p>
 * This may not be as well optimized as would be possible with a specialized
 * implementation for a given engine, but it will probably be pretty close.
 * 
 * @author Elias N Vasylenko
 */
public interface InvocableBase extends Invocable {
	@Override
	default <T> T getInterface(Class<T> clasz) {
		return getInterface(null, clasz);
	}

	@SuppressWarnings("unchecked")
	@Override
	default <T> T getInterface(Object thiz, Class<T> clasz) {
		if (clasz.isInstance(thiz)) {
			return (T) thiz;
		}

		BiFunction<String, Object[], Object> invocation;

		if (thiz == null) {
			invocation = (name, args) -> {
				try {
					return invokeFunction(name, args);
				} catch (NoSuchMethodException | ScriptException e) {
					throw new RuntimeException(e);
				}
			};
		} else {
			invocation = (name, args) -> {
				try {
					return invokeMethod(thiz, name, args);
				} catch (NoSuchMethodException | ScriptException e) {
					throw new RuntimeException(e);
				}
			};
		}

		return (T) newProxyInstance(getClass().getClassLoader(), new Class<?>[] { clasz }, (proxy, method, args) -> {
			return invocation.apply(method.getName(), args);
		});
	}
}
