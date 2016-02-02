package uk.co.strangeskies.utilities.classloader;

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

	public static String PROTOCOL = "deepjar";

	protected static int PROTOCOL_LENGTH = PROTOCOL.length() + 3;

	/**
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		final String resource = url.toString().substring(PROTOCOL_LENGTH);

		return new NestedJarUrlConnection(url);
	}
}
