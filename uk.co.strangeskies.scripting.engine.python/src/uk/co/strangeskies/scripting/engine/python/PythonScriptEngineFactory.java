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
 * This file is part of uk.co.strangeskies.scripting.engine.python.
 *
 * uk.co.strangeskies.scripting.engine.python is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.scripting.engine.python is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.scripting.engine.python;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walk;
import static uk.co.strangeskies.reflection.resource.Jar.getContainingJar;
import static uk.co.strangeskies.reflection.resource.Jar.getJar;

import java.nio.file.Path;

import org.python.jsr223.PyScriptEngineFactory;

@SuppressWarnings("javadoc")
public class PythonScriptEngineFactory extends PyScriptEngineFactory {
	public PythonScriptEngineFactory() {
		Path pythonHome;

		try {
			pythonHome = createTempDirectory("python-home");

			/*
			 * Extract the lib.jar which is nested in the running bundle into a
			 * temporary directory.
			 */
			Path libJar = getContainingJar(getClass()).getRootPath().resolve("lib.jar");
			libJar = copy(libJar, pythonHome.resolve("lib.jar"));

			/*
			 * Extract the contents of the lib.jar into the temporary directory.
			 */
			Path libJarRoot = getJar(libJar.toUri()).getRootPath();
			walk(libJarRoot).filter(p -> !isDirectory(p)).forEach(c -> {
				try {
					Path source = c;
					Path destination = pythonHome.resolve(libJarRoot.relativize(c).toString());

					createDirectories(destination.getParent());
					copy(source, destination);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		System.setProperty("python.console.encoding", "UTF-8");
		System.setProperty("python.home", pythonHome.toFile().toString());
	}
}
