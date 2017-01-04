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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * An immutable register of class signatures to be declared.
 * 
 * <p>
 * Class registers are declared to create a {@link ClassSpace class space},
 * containing {@link ClassDeclaration class declarations} for each signature in
 * the register. We build a registry of class signatures before declaring them
 * as a group so that we may naturally facilitate cycles in the class
 * declaration graph without depending on mutable state.
 * 
 * <p>
 * Classes signatures with no interdependencies may be declared into an
 * anonymous, unit-set register and class space via
 * {@link ClassSignature#defineSingle()}
 * 
 * @author Elias N Vasylenko
 */
public class ClassRegister {
	private final Map<String, ClassSignature<?>> classSignatures;

	public ClassRegister() {
		classSignatures = emptyMap();
	}

	protected ClassRegister(Map<String, ClassSignature<?>> classSignatures) {
		this.classSignatures = classSignatures;
	}

	public ClassRegister withClassSignature(ClassSignature<?> classSignature) {
		Map<String, ClassSignature<?>> classSignatures = new HashMap<>(this.classSignatures);
		if (classSignatures.putIfAbsent(classSignature.getClassName(), classSignature) != null) {
			throw new CodeGenerationException(m -> m.classNameAlreadyRegistered(classSignature));
		}
		return new ClassRegister(classSignatures);
	}

	public ClassSpace declare() {
		return new ClassSpace(this);
	}

	public ClassSignature<?> getClassSignature(String className) {
		return classSignatures.get(className);
	}

	public Stream<ClassSignature<?>> getClassSignatures() {
		return classSignatures.values().stream();
	}

	public Stream<String> getClassNames() {
		return classSignatures.keySet().stream();
	}
}
