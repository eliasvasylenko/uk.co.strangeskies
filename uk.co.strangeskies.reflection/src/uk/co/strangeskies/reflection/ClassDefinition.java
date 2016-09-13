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

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Proxy;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * A class definition is a description of a class implementation. It may extend
 * an existing class, and implement existing interfaces, as well as provide
 * {@link MethodDefinition method implementations} and overrides for those
 * supertypes.
 * 
 * <p>
 * A class definition may be implemented reflectively via {@link Proxy},
 * delegating invocations to direct evaluation of {@link MethodDefinition method
 * definition bodies}, and the{@link Statement statements} therein. A class
 * definition also contains enough information to generate the bytecode for a
 * true implementation, with performance comparable to a regular compiled Java
 * class, though this is not yet implemented.
 * 
 * <p>
 * Aside from this than this, there are currently a number of significant
 * limitations to class definitions:
 * 
 * <ul>
 * <li>Though supertypes may be parameterized generic types, it is not supported
 * for the class definition itself to be a generic type.</li>
 * 
 * <li>It is not supported to declare new methods which are not simply overrides
 * of existing methods. Variance is technically supported where appropriate in
 * method declarations, but the information is not available reflectively so
 * this provides limited utility.</li>
 * 
 * <li>It is possible to define an implementation for a default constructor if
 * necessary, but it is not supported to define constructors with
 * arguments.</li>
 * </ul>
 * 
 * <p>
 * These limitations help to keep class definitions simple, as their type can be
 * effectively reflected over via {@link TypeToken} with no special
 * consideration. Also the need for interdependencies between class definitions
 * should be largely avoided.
 * 
 * <p>
 * There are a few potential paths to evolving this class beyond some or all of
 * these limitations ... But they are likely to require significant work, and
 * may challenge some pervasive assumptions made by the existing library design,
 * for example that the raw type of any type is a {@link Class}.
 * 
 * <p>
 * Every implementation generated via a class definition also be cast to
 * {@link Reified} to reflect over its exact type. The implementation of all
 * relevant methods will be generated automatically <em>where possible</em>. If
 * the {@link Reified} class is overridden explicitly however, or if any method
 * in {@link Reified} is shadowed by an explicitly overridden class, the user
 * should provide their own implementations for these methods.
 * 
 * Note that if supported is added for generation of generic classes in the
 * future, the exact type of such classes may not be determined statically so
 * they will not implement reified by default.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          the intersection of the supertypes of the described class
 */
public class ClassDefinition<T> implements GenericDeclaration {
	/**
	 * Separating the logic for declaring the class into a builder allows us to
	 * ensure the type of the class is immutable once an actual
	 * {@link ProcedureDefinition} object is instantiated. This means that the
	 * type can be safely reasoned about before any class members are defined or
	 * any implementation details are specified.
	 * 
	 * @author Elias N Vasylenko
	 * 
	 * @param <T>
	 *          the intersection of the supertypes of the described class
	 */
	public static class ClassSignature<T> extends GenericSignature {
		private final TypeToken<T> superType;

		public ClassSignature(TypeToken<T> superType) {
			this.superType = superType;
		}

		public ClassDefinition<T> define() {
			return new ClassDefinition<>(this);
		}
	}

	public static ClassSignature<Object> declare() {
		return declare(Object.class);
	}

	public static <T> ClassSignature<T> declare(Class<T> superType) {
		return declare(TypeToken.over(superType));
	}

	public static <T> ClassSignature<T> declare(TypeToken<T> superType) {
		return new ClassSignature<>(superType);
	}

	protected ClassDefinition(ClassSignature<T> builder) {
		superType = builder.superType;
		typeVariables = builder.getTypeVariables(this);
	}

	private final TypeToken<T> superType;
	private final List<TypeVariable<ClassDefinition<T>>> typeVariables;

	public T instantiate(Object... arguments) {
		// TODO may take arguments
		return null;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeVariable<?>[] getTypeParameters() {
		// TODO Auto-generated method stub
		return null;
	}
}
