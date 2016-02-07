/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class NestedJarUrlConnection extends URLConnection {
	private URL delegate;
	private JarFile jarFile;
	private JarEntry rootEntry;

	protected NestedJarUrlConnection(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
		if (connected)
			return;

		delegate = new URL(getURL().toString().substring(7));

		JarURLConnection conn = (JarURLConnection) delegate.openConnection();

		this.jarFile = conn.getJarFile();
		this.rootEntry = conn.getJarEntry();

		connected = true;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (!connected)
			connect();

		return new NestedJarInputStream(jarFile, rootEntry);
	}
}
