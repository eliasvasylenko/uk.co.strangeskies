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
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;

import uk.co.strangeskies.utilities.text.Parser;
import uk.co.strangeskies.utilities.text.StringEscaper;

public class Classpath {
	private static final ManifestEntryParser MANIFEST_ENTRY_PARSER = new ManifestEntryParser();

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

	public interface Entry {
		String name();

		Set<EntryAttribute> attributes();
	}

	public interface EntryAttribute {
		String name();

		PropertyType type();

		Object value();
	}

	private static class EntryImpl implements Entry {
		private final String name;
		private final Set<EntryAttribute> attributes = new HashSet<>();

		public EntryImpl(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public Set<EntryAttribute> attributes() {
			return Collections.unmodifiableSet(attributes);
		}

		public void addAttributes(Collection<? extends EntryAttribute> attributes) {
			this.attributes.addAll(attributes);
		}
	}

	public static class EntryAttributeImpl implements EntryAttribute {
		private final String name;
		private PropertyType type;
		private Object value;

		public EntryAttributeImpl(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public PropertyType type() {
			return type;
		}

		public void setType(PropertyType type) {
			this.type = type;
		}

		@Override
		public Object value() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static enum PropertyType {
		STRING(String.class, "String"),

		STRINGS(String[].class, "List<String>"),

		LONG(long.class, "Long"),

		LONGS(long[].class, "List<Long>"),

		VERSION(null, "Version"),

		VERSIONS(null, "List<Version>"),

		DOUBLE(double.class, "Double"),

		DOUBLES(double[].class, "List<Double>");

		private final Type type;
		private final String string;

		PropertyType(Type type, String toString) {
			this.type = type;
			this.string = toString;
		}

		@Override
		public String toString() {
			return string;
		}

		public static PropertyType fromString(String literal) {
			for (PropertyType value : values()) {
				if (value.toString().equals(literal)) {
					return value;
				}
			}
			throw new IllegalArgumentException("Invalid PropertyType: " + literal);
		}
	}

	private static class ManifestEntryParser {
		private final Parser<Entry> entry;
		private final Parser<EntryAttribute> entryAttribute;
		private final Parser<String> valueString;

		public ManifestEntryParser() {
			Parser<PropertyType> type = Parser.matching("[_a-zA-Z0-9<>]*").transform(PropertyType::fromString);

			Parser<String> singleQuotedString = Parser.matching("([^\\'\\\\]*(\\\\.[^\'\\\\]*)*)").append("\\'")
					.prepend("\\'");

			Parser<String> doubleQuotedString = Parser.matching("([^\"\\\\]*(\\\\.[^\"\\\\]*)*)").append("\"").prepend("\"");

			valueString = Parser.matching("[_a-zA-Z0-9\\.]*")
					.orElse(singleQuotedString.orElse(doubleQuotedString).transform(StringEscaper.java()::unescape));

			entryAttribute = Parser.matching("[_a-zA-Z0-9\\.]*").transform(EntryAttributeImpl::new)
					.append(type.prepend(":").orElse(PropertyType.STRING), EntryAttributeImpl::setType).append("=")
					.append(valueString, EntryAttributeImpl::setValue).transform(e -> (EntryAttribute) e);

			entry = Parser.matching("[_a-zA-Z0-9\\.]*").transform(EntryImpl::new)
					.tryAppend(Parser.list(entryAttribute, ";"), EntryImpl::addAttributes).transform(e -> (Entry) e);
		}

		public Parser<Entry> getEntry() {
			return entry;
		}

		public Parser<EntryAttribute> getEntryAttribute() {
			return entryAttribute;
		}

		public Parser<String> getValueString() {
			return valueString;
		}
	}

	public static String parseValueString(String valueString) {
		return MANIFEST_ENTRY_PARSER.getValueString().parse(valueString);
	}

	public static EntryAttribute parseManifestEntryAttribute(String attribute) {
		return MANIFEST_ENTRY_PARSER.getEntryAttribute().parse(attribute);
	}

	public static Entry parseManifestEntry(String entry) {
		return MANIFEST_ENTRY_PARSER.getEntry().parse(entry);
	}
}
