/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,L__   __||  _ `.        / \     |  \   | |  ,-`__`]  ,-`__`]
 *   ( (_`-`   | |   | | ) |       / . \    | . \  | | / .`  `  / .`  `
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `-`.   / /   \ \  | | \ \| || |   | || +--J
 *  \ \__.` /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,-
 *   `.__.-`   L_|   L_|    L_|/_/       \_\L_|   \__|  `-.__.'  `-.__.]
 *                   __    _         _      __      __
 *                 ,`_ `, | |   _   | |  ,-`__`]  ,`_ `,
 *                ( (_`-` | '-.) |  | | / .`  `  ( (_`-`
 *                 `._ `. | +-. <   | || '--.     `._ `.
 *                _   `. \| |  `-`. | || +--J    _   `. \
 *               \ \__.` /| |    \ \| | \ `.__,-\ \__.` /
 *                `.__.-` L_|    L_|L_|  `-.__.] `.__.-`
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.parsing;

import java.util.function.Function;
import java.util.function.Supplier;

public class ParserProxy<U, T> implements AbstractParser<T> {
	private final Supplier<Parser<U>> component;
	private final Function<? super U, ? extends T> transform;

	public ParserProxy(Parser<U> component,
			Function<? super U, ? extends T> transform) {
		this(() -> component, transform);
	}

	public ParserProxy(Supplier<Parser<U>> component,
			Function<? super U, ? extends T> transform) {
		this.component = component;
		this.transform = transform;
	}

	protected Supplier<Parser<U>> getComponent() {
		return component;
	}

	public Function<? super U, ? extends T> getTransform() {
		return transform;
	}

	@Override
	public <V> Parser<V> transform(Function<? super T, ? extends V> transform) {
		return new ParserProxy<>(component, this.transform.andThen(transform));
	}

	@Override
	public ParseResult<T> parseSubstringImpl(ParseState state) {
		return component.get().parseSubstring(state).mapResult(transform);
	}

	@Override
	public String toString() {
		return "Proxy Parser (" + component + ")";
	}
}
