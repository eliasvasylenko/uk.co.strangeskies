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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Take care not to allow instances of this class to leak out into the wider
 * program outside the context of declaration and definition of
 * {@link ParameterizedDeclaration mutable declarations}.
 * 
 * @author Elias N Vasylenko
 */
public class TypeVariableDeclaration implements Type {
	private final int index;
	private final List<AnnotatedType> bounds;
	private final List<Annotation> annotations;

	/**
	 * @param index
	 *          the index of the type variable signature within its generic
	 *          declaration
	 */
	public TypeVariableDeclaration(int index) {
		this.index = index;
		bounds = new ArrayList<>();
		annotations = new ArrayList<>();
	}

	/**
	 * @return the index of the type variable signature within its generic
	 *         declaration
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public String getTypeName() {
		return "T" + index;
	}

	public List<AnnotatedType> getBounds() {
		return bounds;
	}

	public TypeVariableDeclaration withUpperBounds(TypeToken<?>... bounds) {
		return withUpperBounds(Arrays.stream(bounds).map(TypeToken::getAnnotatedDeclaration).collect(Collectors.toList()));
	}

	public TypeVariableDeclaration withUpperBounds(Type... bounds) {
		return withUpperBounds(Arrays.stream(bounds).map(AnnotatedTypes::over).collect(Collectors.toList()));
	}

	public TypeVariableDeclaration withUpperBounds(AnnotatedType... bounds) {
		return withUpperBounds(Arrays.asList(bounds));
	}

	public TypeVariableDeclaration withUpperBounds(Collection<? extends AnnotatedType> bounds) {
		this.bounds.addAll(bounds);

		return this;
	}

	public TypeVariableDeclaration withAnnotations(Annotation... annotations) {
		return withAnnotations(Arrays.asList(annotations));
	}

	public TypeVariableDeclaration withAnnotations(Collection<? extends Annotation> annotations) {
		this.annotations.addAll(annotations);

		return this;
	}
}
