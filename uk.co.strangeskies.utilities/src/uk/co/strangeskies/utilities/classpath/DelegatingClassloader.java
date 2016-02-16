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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class DelegatingClassloader extends ClassLoader {
	private static final int CHUNK_SIZE = 2048;

	private List<ClassLoader> delegateClassLoaders;

	public DelegatingClassloader(ClassLoader parent, ClassLoader... delegateClassLoaders) {
		this(parent, Arrays.asList(delegateClassLoaders));
	}

	public DelegatingClassloader(ClassLoader parent, Collection<? extends ClassLoader> delegateClassLoaders) {
		super(parent);
		this.delegateClassLoaders = new ArrayList<>(delegateClassLoaders);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		for (ClassLoader loader : delegateClassLoaders) {
			try {
				return loader.loadClass(name);
			} catch (ClassNotFoundException e) {}
		}
		throw new ClassNotFoundException(
				"Class '" + name + "' not found in any classloader '" + getParent() + "' or '" + delegateClassLoaders + "'");
	}

	private ByteBuffer loadResource(URL url) throws IOException {
		URLConnection connection = url.openConnection();
		InputStream in = connection.getInputStream();
		int contentLength = connection.getContentLength();

		ByteArrayOutputStream tmpOut;
		if (contentLength != -1) {
			tmpOut = new ByteArrayOutputStream(contentLength);
		} else {
			tmpOut = new ByteArrayOutputStream(CHUNK_SIZE);
		}

		byte[] buf = new byte[CHUNK_SIZE];
		while (true) {
			int len = in.read(buf);
			if (len == -1) {
				break;
			}
			tmpOut.write(buf, 0, len);
		}
		in.close();
		tmpOut.close();

		byte[] array = tmpOut.toByteArray();

		return ByteBuffer.wrap(array);
	}

	@Override
	protected URL findResource(String name) {
		for (ClassLoader delegate : delegateClassLoaders) {
			URL resource = delegate.getResource(name);
			if (resource != null)
				return resource;
		}
		return null;
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		Vector<URL> vector = new Vector<URL>();
		for (ClassLoader delegate : delegateClassLoaders) {
			Enumeration<URL> enumeration = delegate.getResources(name);
			while (enumeration.hasMoreElements()) {
				vector.add(enumeration.nextElement());
			}
		}
		return vector.elements();
	}
}
