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
public class ProcedureDefinition<T> extends BlockBuilder<ProcedureDefinition<T>> {
	/**
	 * Separating the logic for declaring input parameters into a builder means
	 * that we can ensure the argument and return types are immutable once an
	 * actual {@link ProcedureDefinition} object is instantiated. This means that
	 * the type
	 * 
	 * @author Elias N Vasylenko
	 */
	public static class ProcedureSignature<T> {
		private static final AtomicLong COUNT = new AtomicLong(0);
		private final ClassSignature<Procedure<T>> classDeclarationBuilder;
		private TypeToken<T> resultType;

		@SuppressWarnings("unchecked")
		protected ProcedureSignature() {
			long count = COUNT.incrementAndGet();

			classDeclarationBuilder = ClassDefinition.declare(ProcedureDefinition.class.getName() + "$" + count)
					.withSuperType(new TypeToken<Procedure<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, resultType));

			resultType = (TypeToken<T>) TypeToken.over(Object.class);
		}

		protected TypeToken<T> getResultType() {
			return resultType;
		}

		public <U extends T> ProcedureSignature<U> withResultType(Class<U> resultType) {
			return withResultType(TypeToken.over(resultType));
		}

		@SuppressWarnings("unchecked")
		public <U extends T> ProcedureSignature<U> withResultType(TypeToken<U> resultType) {
			this.resultType = (TypeToken<T>) resultType;
			return (ProcedureSignature<U>) this;
		}

		public ProcedureDefinition<T> define() {
			return new ProcedureDefinition<>(this);
		}
	}

	public static ProcedureSignature<Object> declare() {
		return new ProcedureSignature<>();
	}

	private final ClassDefinition<Procedure<T>> classDefinition;

	protected ProcedureDefinition(ProcedureSignature<T> builder) {
		classDefinition = builder.classDeclarationBuilder.define();

	}

	public Procedure<T> instantiate() {
		return classDefinition.instantiate();
	}
}
