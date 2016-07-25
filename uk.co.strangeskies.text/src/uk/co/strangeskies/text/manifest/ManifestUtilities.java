/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.manifest;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.jar.Manifest;

public class ManifestUtilities {
	private static final ManifestAttributeParser MANIFEST_ENTRY_PARSER = new ManifestAttributeParser();

	private ManifestUtilities() {}

	public static Manifest getManifest(Class<?> clz) {
		String resource = "/" + clz.getName().replace('.', '/') + ".class";
		String fullPath = clz.getResource(resource).toString();
		String archivePath = fullPath.substring(0, fullPath.length() - resource.length());

		/*
		 * Deal with Wars
		 */
		if (archivePath.endsWith("\\WEB-INF\\classes") || archivePath.endsWith("/WEB-INF/classes")) {
			archivePath = archivePath.substring(0, archivePath.length() - "/WEB-INF/classes".length());
		}

		try (InputStream input = new URL(archivePath + "/META-INF/MANIFEST.MF").openStream()) {
			return new Manifest(input);
		} catch (Exception e) {
			throw new RuntimeException("Loading MANIFEST for class " + clz + " failed", e);
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
