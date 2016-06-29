/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.text.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;
import uk.co.strangeskies.text.properties.PropertyConfiguration.Requirement;

class PropertyResourceConfiguration<T extends Properties<T>> {
	@PropertyConfiguration
	private final class DefaultPropertyConfigurationAnnotation {}

	private final Class<T> accessor;
	private final PropertyConfiguration configuration;

	protected PropertyResourceConfiguration(Class<T> accessor) {
		this(accessor, accessor.getAnnotation(PropertyConfiguration.class));
	}

	protected PropertyResourceConfiguration(Class<T> accessor, PropertyConfiguration configuration) {
		this.accessor = accessor;
		this.configuration = configuration != null ? configuration
				: DefaultPropertyConfigurationAnnotation.class.getAnnotation(PropertyConfiguration.class);
	}

	public <A extends Properties<A>> PropertyResourceConfiguration<A> derive(Class<A> accessor) {
		return new PropertyResourceConfiguration<>(accessor,
				deriveConfiguration(accessor.getAnnotation(PropertyConfiguration.class)));
	}

	public PropertyResourceConfiguration<T> derive(PropertyConfiguration configuration) {
		if (configuration != null) {
			return new PropertyResourceConfiguration<>(accessor, deriveConfiguration(configuration));
		} else {
			return this;
		}
	}

	public Class<T> getAccessor() {
		return accessor;
	}

	public PropertyConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof PropertyResourceConfiguration))
			return false;

		PropertyResourceConfiguration<?> other = (PropertyResourceConfiguration<?>) obj;

		return accessor.equals(other.accessor) && configuration.equals(other.configuration);
	}

	@Override
	public int hashCode() {
		return accessor.hashCode() ^ configuration.hashCode();
	}

	PropertyConfiguration deriveConfiguration(PropertyConfiguration configuration) {
		Class<? extends PropertyResourceStrategy> strategy;
		String resource;
		Requirement requirement;
		String keySplitString;
		KeyCase keyCase;
		String key;

		boolean merged = false;

		if (configuration.strategy().equals(PropertyConfiguration.UnspecifiedPropertyResourceStrategy.class)) {
			merged = true;
			strategy = getConfiguration().strategy();
		} else {
			strategy = configuration.strategy();
		}

		if (configuration.resource().equals(PropertyConfiguration.UNSPECIFIED_RESOURCE)) {
			merged = true;
			resource = getConfiguration().resource();
		} else {
			resource = configuration.resource();
		}

		if (configuration.requirement().equals(Requirement.UNSPECIFIED)) {
			merged = true;
			requirement = getConfiguration().requirement();
		} else {
			requirement = configuration.requirement();
		}

		if (configuration.keySplitString().equals(PropertyConfiguration.UNSPECIFIED_KEY)) {
			merged = true;
			keySplitString = getConfiguration().keySplitString();
		} else {
			keySplitString = configuration.keySplitString();
		}

		if (configuration.keyCase().equals(KeyCase.UNSPECIFIED)) {
			merged = true;
			keyCase = getConfiguration().keyCase();
		} else {
			keyCase = configuration.keyCase();
		}

		if (configuration.key().equals(PropertyConfiguration.UNSPECIFIED_KEY)) {
			merged = true;
			key = getConfiguration().key();
		} else {
			key = configuration.key();
		}

		if (merged) {
			configuration = new PropertyConfiguration() {
				@Override
				public Class<PropertyConfiguration> annotationType() {
					return PropertyConfiguration.class;
				}

				@Override
				public Class<? extends PropertyResourceStrategy> strategy() {
					return strategy;
				}

				@Override
				public String resource() {
					return resource;
				}

				@Override
				public Requirement requirement() {
					return requirement;
				}

				@Override
				public String keySplitString() {
					return keySplitString;
				}

				@Override
				public KeyCase keyCase() {
					return keyCase;
				}

				@Override
				public String key() {
					return key;
				}

				@Override
				public int hashCode() {
					try {
						int hashCode = 0;
						for (Method propertyMethod : PropertyConfiguration.class.getDeclaredMethods()) {
							int nameHash = propertyMethod.getName().hashCode() * 127;
							Object valueHash = propertyMethod.invoke(this);

							hashCode += nameHash ^ valueHash.hashCode();
						}
						return hashCode;
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public boolean equals(Object obj) {
					try {
						for (Method propertyMethod : PropertyConfiguration.class.getDeclaredMethods()) {
							if (!Objects.equals(propertyMethod.invoke(this), propertyMethod.invoke(obj))) {
								return false;
							}
						}
						return true;
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			};
		}

		return configuration;
	}
}
