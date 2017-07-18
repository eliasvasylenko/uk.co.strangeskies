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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import uk.co.strangeskies.reflection.codegen.block.Block;

public class MethodDefinition<C, T> implements MemberDefinition<C> {
	private final MethodDeclaration<C, T> methodDeclaration;
	private final Block<? extends T> body;

	public MethodDefinition(MethodDeclaration<C, T> classDeclaration) {
		this.methodDeclaration = classDeclaration;
		this.body = new Block<>();
	}

	private MethodDefinition(MethodDefinition<C, T> definition, Block<? extends T> body) {
		this.methodDeclaration = definition.getDeclaration();
		this.body = body;
	}

	public Block<? extends T> body() {
		return body;
	}

	public MethodDeclaration<C, T> getDeclaration() {
		return methodDeclaration;
	}

	@Override
	public ClassDeclaration<?, C> getOwningClassDeclaration() {
		return getDeclaration().getOwningDeclaration();
	}

	@Override
	public ClassDeclaration<?, ?> getClassDeclaration() {
		return getDeclaration().getDeclaringClass();
	}

	public MethodDefinition<C, T> withBody(Block<? extends T> body) {
		return new MethodDefinition<>(this, body);
	}
}
