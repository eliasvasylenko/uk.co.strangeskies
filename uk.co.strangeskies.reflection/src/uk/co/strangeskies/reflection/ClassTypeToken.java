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

import java.lang.reflect.WildcardType;

public class ClassTypeToken<T> extends TypeToken<T> {

	public boolean isAbstract() {
		return Types.isAbstract(getRawType());
	}

	public boolean isFinal() {
		return Types.isFinal(getRawType());
	}

	public boolean isInterface() {
		return Types.isInterface(getRawType());
	}

	public boolean isPrivate() {
		return Types.isPrivate(getRawType());
	}

	public boolean isProtected() {
		return Types.isProtected(getRawType());
	}

	public boolean isPublic() {
		return Types.isPublic(getRawType());
	}

	public boolean isStatic() {
		return Types.isStatic(getRawType());
	}

	public boolean isWildcard() {
		return getType() instanceof WildcardType;
	}

	public Class<?> getNonStaticallyEnclosingClass() {
		return Types.getNonStaticallyEnclosingClass(getRawType());
	}

}
