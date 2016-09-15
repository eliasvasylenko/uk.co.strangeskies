/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

import uk.co.strangeskies.reflection.ClassDefinition.ClassSignature;

/**
 * Conceptually a procedure is a stand-alone block of statements outside of the
 * scope of any {@link ClassDefinition class definition}. This simply provides a
 * basic means to declaratively define a process for reflective execution over a
 * set of input {@link VariableExpression variables}, and one output variable.
 * 
 * @author Elias N Vasylenko
 *
 */
public class ProcedureDefinition<T> {
	private static final AtomicLong COUNT = new AtomicLong(0);

	public static ProcedureDefinition<?> define(Type resultType) {
		return define(TypeToken.over(resultType));
	}

	public static ProcedureDefinition<?> define(AnnotatedType resultType) {
		return define(TypeToken.over(resultType));
	}

	public static <U> ProcedureDefinition<U> define(Class<U> resultType) {
		return define(TypeToken.over(resultType));
	}

	public static <U> ProcedureDefinition<U> define(TypeToken<U> resultType) {
		return new ProcedureDefinition<>(resultType);
	}

	private final ClassDefinition<? extends Procedure<T>> classDefinition;
	private final MethodDefinition<? extends Procedure<T>, T> runMethodDefinition;

	protected ProcedureDefinition(TypeToken<T> resultType) {
		long count = COUNT.incrementAndGet();

		ClassSignature<Procedure<T>> classSignature = ClassDefinition
				.declareClass(ProcedureDefinition.class.getName() + "$" + count)
				.withSuperType(new TypeToken<Procedure<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, resultType));

		classDefinition = classSignature.define();
		runMethodDefinition = classDefinition.declareMethod("execute").withReturnType(resultType).define();
	}

	public Procedure<T> instantiate() {
		return classDefinition.instantiate();
	}

	public TypedBlockDefinition<T> body() {
		return runMethodDefinition.body();
	}
}
