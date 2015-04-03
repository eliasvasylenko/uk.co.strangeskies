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
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ParameterizedTypeToken<T> extends TypeToken<T> {
	@SuppressWarnings("unchecked")
	public ParameterizedTypeToken(ParameterizedType type) {
		super(null, type, (Class<? super T>) type.getRawType());

		validate();
	}

	public static ParameterizedTypeToken<?> of(ParameterizedType type) {
		return new ParameterizedTypeToken<>(type);
	}

	public List<TypeVariable<?>> getAllTypeParameters() {
		return ParameterizedTypes.getAllTypeParameters(getRawType());
	}

	public Map<TypeVariable<?>, Type> getAllTypeArguments() {
		if (getType() instanceof ParameterizedType)
			return ParameterizedTypes
					.getAllTypeArguments((ParameterizedType) getType());
		else
			return Collections.emptyMap();
	}
}
