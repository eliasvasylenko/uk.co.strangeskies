/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. l      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' l   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

public class FieldDefinition<C, T> implements MemberDefinition {
	private final ClassDefinition<C> classDefinition;
	private final String fieldName;

	private final TypeToken<T> type;
	private final ValueExpression<? extends T> initializer;

	@SuppressWarnings("unchecked")
	protected FieldDefinition(FieldDeclaration<C, T> declaration) {
		this.classDefinition = declaration.getClassDefinition();
		this.fieldName = declaration.getName();
		this.initializer = declaration.getInitializer();

		if (declaration.getType() == null) {
			this.type = (TypeToken<T>) initializer.getType();
		} else {
			this.type = (TypeToken<T>) TypeToken.over(declaration.getType());
		}
	}

	@Override
	public ClassDefinition<C> getDeclaringClassDefinition() {
		return classDefinition;
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public int getModifiers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSynthetic() {
		// TODO Auto-generated method stub
		return false;
	}

}
