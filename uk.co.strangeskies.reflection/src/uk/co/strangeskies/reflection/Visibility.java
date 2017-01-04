/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.lang.reflect.Modifier;
import java.util.Optional;

public enum Visibility {
	PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE;

	public boolean visibilityIsAtLeast(Visibility visibility) {
		return ordinal() <= visibility.ordinal();
	}

	public boolean visibilityIsAtMost(Visibility visibility) {
		return ordinal() >= visibility.ordinal();
	}

	public Optional<String> getKeyword() {
		switch (this) {
		case PUBLIC:
			return of("public");
		case PROTECTED:
			return of("protected");
		case PACKAGE_PRIVATE:
			return empty();
		case PRIVATE:
			return of("private");
		default:
			throw new AssertionError();
		}
	}

	public static Visibility forModifiers(int modifiers) {
		if (Modifier.isPublic(modifiers)) {
			return Visibility.PUBLIC;
		} else if (Modifier.isProtected(modifiers)) {
			return Visibility.PROTECTED;
		} else if (Modifier.isPrivate(modifiers)) {
			return Visibility.PRIVATE;
		} else {
			return Visibility.PACKAGE_PRIVATE;
		}
	}
}
