/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.jar;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;

public class JarUtilities {
	private static final ManifestAttributeParser MANIFEST_ENTRY_PARSER = new ManifestAttributeParser();
	private static final String MANIFEST_PATH = "/META-INF/MANIFEST.MF";

	private JarUtilities() {}

	public static URL getContainingJarLocation(Class<?> clz) {
		try {
			return clz.getProtectionDomain().getCodeSource().getLocation();
		} catch (Exception e) {
			throw new RuntimeException("Loading MANIFEST for class " + clz + " failed", e);
		}
	}

	public static FileSystem getContainingJarFileSystem(Class<?> clz) {
		return getJarFileSystem(getContainingJarLocation(clz));
	}

	public static FileSystem getJarFileSystem(URL jarLocation) {
		try {
			if (!jarLocation.toString().startsWith("jar:")) {
				jarLocation = new URL("jar:" + jarLocation);
			}
			return FileSystems.newFileSystem(jarLocation.toURI(), Collections.emptyMap());
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("Creating file system for jar " + jarLocation + " failed", e);
		}
	}

	public static Manifest getContainingJarManifest(Class<?> clz) {
		return getManifest(getContainingJarFileSystem(clz));
	}

	public static Manifest getJarManifest(URL jarLocation) {
		return getManifest(getJarFileSystem(jarLocation));
	}

	public static Manifest getManifest(FileSystem jarFileSystem) {
		try {
			return new Manifest(Files.newInputStream(jarFileSystem.getPath(MANIFEST_PATH)));
		} catch (IOException e) {
			throw new RuntimeException("Loading MANIFEST for jar " + jarFileSystem + " failed", e);
		}
	}

	public static String parseValueString(String valueString) {
		return MANIFEST_ENTRY_PARSER.parseValueString(valueString);
	}

	public static AttributeProperty<?> parseAttributeProperty(String attribute) {
		return MANIFEST_ENTRY_PARSER.parseAttributeProperty(attribute);
	}

	public static Attribute parseAttribute(String entry) {
		return MANIFEST_ENTRY_PARSER.parseAttribute(entry);
	}

	public static List<Attribute> parseAttributes(String entry) {
		return MANIFEST_ENTRY_PARSER.parseAttributes(entry);
	}
}
