/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class TypeVisitor {
	private final boolean allowRepeatVisits;
	private final Set<Type> visited = new HashSet<>();

	public TypeVisitor() {
		this(false);
	}

	public TypeVisitor(boolean allowRepeatVisits) {
		this.allowRepeatVisits = allowRepeatVisits;
	}

	public synchronized final void visit(Type... types) {
		visit(Arrays.asList(types));
	}

	public synchronized final void visit(Iterable<? extends Type> types) {
		for (Type type : types) {
			if (visited.add(type)) {
				if (type instanceof Class)
					visitClass((Class<?>) type);
				else if (type instanceof ParameterizedType)
					visitParameterizedType((ParameterizedType) type);
				else if (type instanceof GenericArrayType)
					visitGenericArrayType((GenericArrayType) type);
				else if (type instanceof WildcardType)
					visitWildcardType((WildcardType) type);
				else if (type instanceof IntersectionType)
					visitIntersectionType((IntersectionType) type);
				else if (type instanceof TypeVariableCapture)
					visitTypeVariableCapture((TypeVariableCapture) type);
				else if (type instanceof TypeVariable)
					visitTypeVariable((TypeVariable<?>) type);
				else if (type instanceof InferenceVariable)
					visitInferenceVariable((InferenceVariable) type);
				else if (type == null)
					visitNull();
				else
					throw new AssertionError("Unknown type: " + type + " of class "
							+ type.getClass());

				if (allowRepeatVisits)
					visited.remove(type);
			}
		}
	}

	protected void visitNull() {}

	protected void visitClass(Class<?> type) {}

	protected void visitParameterizedType(ParameterizedType type) {}

	protected void visitGenericArrayType(GenericArrayType type) {}

	protected void visitWildcardType(WildcardType type) {}

	protected void visitTypeVariableCapture(TypeVariableCapture type) {}

	protected void visitTypeVariable(TypeVariable<?> type) {}

	protected void visitInferenceVariable(InferenceVariable type) {}

	protected void visitIntersectionType(IntersectionType type) {}
}
