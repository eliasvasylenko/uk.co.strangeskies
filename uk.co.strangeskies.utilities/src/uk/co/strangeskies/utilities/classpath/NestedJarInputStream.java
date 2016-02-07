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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class NestedJarInputStream extends InputStream {
	private PipedInputStream pipedInputStream;
	private PipedOutputStream pipedOutputStream;

	private Thread writingThread;

	protected NestedJarInputStream(final JarFile jarFile, final JarEntry rootEntry) throws IOException {
		this.pipedInputStream = new PipedInputStream();
		this.pipedOutputStream = new PipedOutputStream(pipedInputStream);

		Manifest mf = jarFile.getManifest();
		final JarOutputStream jos;
		if (mf != null)
			jos = new JarOutputStream(pipedOutputStream, mf);
		else
			jos = new JarOutputStream(pipedOutputStream);

		this.writingThread = new Thread() {
			@Override
			public void run() {
				try {
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (isRelativeTo(rootEntry, entry)) {
							InputStream in = jarFile.getInputStream(entry);
							try {
								JarEntry newEntry = createEntry(rootEntry, entry);
								jos.putNextEntry(newEntry);

								copy(in, jos, entry.getSize());

								jos.closeEntry();
							} finally {
								in.close();
							}
						}
					}
					jos.flush();
					jos.finish();
				} catch (IOException e) {
					// FIXME: How to handle this?
					//log.warn(e.getMessage(), e);
				}
			}
		};
		writingThread.start();
	}

	@Override
	public void close() throws IOException {
		pipedOutputStream.close();
		pipedInputStream.close();
		// TODO: rejoin the thread?
		// writingThread.interrupt();
		// writingThread.join(5000);
		super.close();
	}

	protected void copy(InputStream in, OutputStream out, long size) throws IOException {
		byte buf[] = new byte[65536];

		while (size > 0) {
			int len = in.read(buf);
			if (len < 0)
				throw new EOFException("Unexpected EOF");
			out.write(buf, 0, len);
			size -= len;
		}
	}

	protected JarEntry createEntry(JarEntry rootEntry, JarEntry template) {
		if (rootEntry == null)
			return new JarEntry(template);

		String name = template.getName().substring(rootEntry.getName().length());
		JarEntry entry = new JarEntry(name);
		entry.setComment(template.getComment());
		entry.setSize(template.getSize());
		entry.setTime(template.getTime());
		return entry;
	}

	protected boolean isRelativeTo(JarEntry rootEntry, JarEntry current) {
		if (rootEntry == null)
			return true;

		if (current.getName().startsWith(rootEntry.getName()) && !current.getName().equals(rootEntry.getName()))
			return true;

		return false;
	}

	@Override
	public int read() throws IOException {
		return pipedInputStream.read();
	}
}
