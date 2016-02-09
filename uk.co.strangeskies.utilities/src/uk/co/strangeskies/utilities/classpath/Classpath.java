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

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

public class Classpath {
	private Classpath() {}

	public static Manifest getManifest(Class<?> clz) {
		String resource = "/" + clz.getName().replace(".", "/") + ".class";
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
			throw new RuntimeException("Loading MANIFEST for class " + clz + " failed!", e);
		}
	}

	public static Map<String, Map<String, String>> parseManifestEntry(String entry) {
		Map<String, Map<String, String>> items = new HashMap<>();

		for (String item : entry.split(",")) {
			int separatorIndex = item.indexOf(";");
			if (separatorIndex >= 0) {
				String itemName = item.substring(0, separatorIndex).trim();

				if (itemName.contains(":")) {
					itemName = itemName.substring(0, itemName.indexOf(":")).trim();
				}

				Map<String, String> properties = new HashMap<>();

				String propertiesString = item.substring(separatorIndex + 1).trim();

				for (String property : propertiesString.split(";")) {
					int equalsIndex = property.indexOf("=");
					if (equalsIndex >= 0) {
						String propertyName = property.substring(0, equalsIndex).trim();
						String propertyString = property.substring(equalsIndex + 1).trim();

						if (propertyString.startsWith("\"") && propertyString.endsWith("\"")) {
							propertyString = propertyString.substring(1, propertyString.length() - 1);
						}

						properties.put(propertyName, propertyString);
					}
				}

				items.put(itemName, properties);
			} else {
				items.put(item.trim(), new HashMap<>());
			}
		}

		return items;
	}
}
