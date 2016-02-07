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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class NestedJarUrlStreamHandler extends URLStreamHandler {
	ClassLoader parent;

	public NestedJarUrlStreamHandler(ClassLoader parent) {
		this.parent = parent;
	}

	public NestedJarUrlStreamHandler() {
		super();
	}

	public static final String PROTOCOL = "jarjar";

	public static final int PROTOCOL_LENGTH = PROTOCOL.length() + 3;

	/**
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		final String resource = url.toString().substring(PROTOCOL_LENGTH);

		return new NestedJarUrlConnection(url);
	}
}
