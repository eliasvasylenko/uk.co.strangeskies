package uk.co.strangeskies.utilities.classloader;

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
