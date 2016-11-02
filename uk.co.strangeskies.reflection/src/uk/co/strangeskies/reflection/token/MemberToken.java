/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.token;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collection;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.TypeResolver;

/**
 * A type safe wrapper around {@link Member} instances, with proper handling of
 * members on generic classes.
 * 
 * <p>
 * {@link MemberToken type members} may be created over types which mention
 * inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the owner type of the member
 */
public interface MemberToken<O> {
	/**
	 * @return the name of the member
	 */
	String getName();

	/**
	 * @return the actual {@link Member} object backing the {@link MemberToken}.
	 */
	Member getMember();

	/**
	 * @return a copy of the {@link TypeResolver} instance backing this
	 *         {@link MemberToken}
	 */
	TypeResolver getResolver();

	/**
	 * @return true if the wrapped member is final, false otherwise
	 */
	boolean isFinal();

	/**
	 * @return true if the wrapped member is private, false otherwise
	 */
	boolean isPrivate();

	/**
	 * @return true if the wrapped member is protected, false otherwise
	 */
	boolean isProtected();

	/**
	 * @return true if the wrapped member is public, false otherwise
	 */
	boolean isPublic();

	/**
	 * @return true if the wrapped member is static, false otherwise
	 */
	boolean isStatic();

	/**
	 * @return The exact declaring class of this member.
	 */
	default Class<?> getDeclaringClass() {
		return getMember().getDeclaringClass();
	}

	/**
	 * This is the exact instance type of this member, and may be parameterized.
	 * <p>
	 * For instance methods, this should be a subtype of the raw
	 * {@link #getDeclaringClass() declaring class}. For constructors of inner
	 * classes, or static fields of inner classes, the instance should be a
	 * subtype of the enclosing class.
	 * <p>
	 * Static members and constructors of static classes have no receiver type.
	 * 
	 * @return a type token over the receiver type for invocation or field access,
	 *         or over <code>void.class</code> if the member has no receiver type
	 */
	TypeToken<O> getReceiverType();

	/**
	 * Derive a new {@link MemberToken} instance, with the given bounds
	 * incorporated into the bounds of the underlying resolver. The original
	 * {@link MemberToken} will remain unmodified.
	 * 
	 * @param bounds
	 *          the new bounds to incorporate
	 * @return the newly derived {@link MemberToken}
	 */
	MemberToken<O> withBounds(BoundSet bounds);

	/**
	 * Derive a new {@link MemberToken} instance, with the bounds on the given
	 * inference variables, with respect to the given bound set, incorporated into
	 * the bounds of the underlying resolver. The original {@link MemberToken}
	 * will remain unmodified.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @param inferenceVariables
	 *          The new inference variables whose bounds are to be incorporated.
	 * @return The newly derived {@link MemberToken}.
	 */
	MemberToken<O> withBounds(BoundSet bounds, Collection<? extends InferenceVariable> inferenceVariables);

	/**
	 * Derive a new {@link MemberToken} instance, with the bounds on the given
	 * type incorporated into the bounds of the underlying resolver. The original
	 * {@link MemberToken} will remain unmodified.
	 * 
	 * @param type
	 *          The type whose bounds are to be incorporated.
	 * @return The newly derived {@link MemberToken}.
	 */
	MemberToken<O> withBoundsFrom(TypeToken<?> type);

	/**
	 * Derive a new instance of {@link MemberToken} with the given owner type.
	 * 
	 * <p>
	 * The new {@link MemberToken} will always have a owner type which is as or
	 * more specific than both the current receiver type <em>and</em> the given
	 * type. This means that the new owner will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> receiver
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param <U>
	 *          The new owner type. The raw type of this type must be a subtype of
	 *          the raw type of the current owner type.
	 * @param type
	 *          The new owner type. The raw type of this type must be a subtype of
	 *          the raw type of the current receiver type.
	 * @return A new {@link MemberToken} compatible with the given owner type.
	 * 
	 *         <p>
	 *         The new owner type will not be effectively more specific than the
	 *         intersection type of the current owner type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current owner type, will also be assignable to the new type.
	 */
	<U extends O> MemberToken<U> withReceiverType(TypeToken<U> type);

	/**
	 * Derive a new instance of {@link MemberToken} with the given owner type.
	 * 
	 * <p>
	 * The new {@link MemberToken} will always have a owner type which is as or
	 * more specific than both the current receiver type <em>and</em> the given
	 * type. This means that the new owner will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> receiver
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param type
	 *          The new owner type. The raw type of this type must be a subtype of
	 *          the raw type of the current receiver type.
	 * @return A new {@link MemberToken} compatible with the given owner type.
	 * 
	 *         <p>
	 *         The new owner type will not be effectively more specific than the
	 *         intersection type of the current owner type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current owner type, will also be assignable to the new type.
	 */
	MemberToken<? extends O> withReceiverType(Type type);

	/**
	 * Derived a new {@link MemberToken} instance with all associated generic
	 * parameters inferred.
	 * 
	 * @return the derived {@link MemberToken} with inferred types
	 */
	MemberToken<O> infer();
}