/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * An inference variable can be thought of as a placeholder for an
 * <em>instantiation</em> of a {@link TypeVariable} of which we do not yet know
 * the exact type. An {@link InferenceVariable} alone has no bounds or type
 * information attached to it. Instead, typically, they are contained within the
 * context of one or more {@link BoundSet}s, which will track bounds on a set of
 * inference variables such they their exact type can ultimately be inferred.
 * 
 * 
 * @author Elias N Vasylenko
 */
public class InferenceVariable implements Type {
	private static final AtomicLong COUNTER = new AtomicLong();

	private final String name;
	private final long number;

	/**
	 * Create a new inference variable with a basic generated name, which is
	 * contained within this bound set.
	 */
	public InferenceVariable() {
		this("INF");
	}

	/**
	 * Create a new inference variable with the given name.
	 * 
	 * @param name
	 *          A name to assign to a new inference variable.
	 */
	public InferenceVariable(String name) {
		this.name = name;
		number = COUNTER.incrementAndGet();
	}

	/**
	 * @return The given name of the inference variable.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The internally assigned number which uniquely distinguishes the
	 *         inference variable from others with the same name.
	 */
	public long getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return name + "#" + number;
	}

	/**
	 * Create fresh {@link InferenceVariable}s for each parameter of the given
	 * type - and each non-statically enclosing type thereof - which is a
	 * {@link WildcardType}. New bounds based on the bounds of those wildcards,
	 * and the bounds of the {@link TypeVariable}s they substitute, will be
	 * incorporated into the given {@link BoundSet}, along with a
	 * {@link CaptureConversion} bound representing this capture conversion. The
	 * process of capture conversion is described in more detail in the Java 8
	 * language specification.
	 * 
	 * @param type
	 *          A parameterised type whose wildcard type arguments, if present, we
	 *          wish to capture as inference variables.
	 * @param bounds
	 *          The bound set we wish to create any fresh inference variables
	 *          within, and incorporate any newly implied bounds into.
	 * @return A new parameterized type derived from the given parameterized type,
	 *         with any fresh {@link InferenceVariable}s substituted for the type
	 *         arguments.
	 */
	/*
	 * Let G name a generic type declaration (§8.1.2, §9.1.2) with n type
	 * parameters A1,...,An with corresponding bounds U1,...,Un.
	 */
	public static ParameterizedType captureConversion(ParameterizedType type,
			BoundSet bounds) {
		if (ParameterizedTypes.getAllTypeArguments(type).values().stream()
				.anyMatch(WildcardType.class::isInstance)) {
			/*
			 * There exists a capture conversion from a parameterized type
			 * G<T1,...,Tn> (§4.5) to a parameterized type G<S1,...,Sn>, where, for 1
			 * ≤ i ≤ n :
			 */

			CaptureConversion captureConversion = new CaptureConversion(type);

			bounds.incorporate().captureConversion(captureConversion);

			return captureConversion.getCaptureType();
		} else
			return type;
	}
}
