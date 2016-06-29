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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for sub-interfaces of {@link Properties}, and for methods on those
 * interfaces, to specify how property resources should be fetched to back the
 * implementation of the accessor.
 * 
 * @author Elias N Vasylenko
 */
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface PropertyConfiguration {
	/**
	 * Property key string case setting.
	 * 
	 * @author Elias N Vasylenko
	 */
	enum KeyCase {
		/**
		 * All upper case.
		 */
		UPPER,

		/**
		 * All lower case.
		 */
		LOWER,

		/**
		 * Preserve original case.
		 */
		PRESERVED,

		/**
		 * Inherit the case settings, or {@link #PRESERVED} by default.
		 */
		UNSPECIFIED
	}

	/**
	 * Property value requirement setting.
	 * 
	 * @author Elias N Vasylenko
	 */
	enum Requirement {
		/**
		 * Verify that a value is available for the property in the root locale
		 * during construction of a {@link Properties} instance, and fail the
		 * request with an exception if it is not.
		 */
		IMMEDIATE,

		/**
		 * Verify that a value is available for the property in the root locale upon
		 * invocation of the accessing method of a {@link Properties} instance, and
		 * fail the request with an exception if it is not.
		 */
		DEFERRED,

		/**
		 * Behave as with {@link #DEFERRED}, with the additional stipulation that a
		 * value may still be considered available if the {@link PropertyProvider
		 * provider} is able to instantiate a default value.
		 * <p>
		 * This is the default behaviour.
		 */
		OPTIONAL,

		/**
		 * Behave as with {@link #OPTIONAL}, except if a value is unavailable, even
		 * by way of a default, simply return null instead of throwing an exception.
		 */
		NULLABLE,

		/**
		 * Inherit the requirement settings, or {@link #OPTIONAL} by default.
		 */
		UNSPECIFIED
	}

	/**
	 * A marker class to signify no resource strategy is specified and should be
	 * inherited if possible.
	 * 
	 * @author Elias N Vasylenko
	 */
	abstract class UnspecifiedPropertyResourceStrategy implements PropertyResourceStrategy {
		private UnspecifiedPropertyResourceStrategy() {}
	}

	/**
	 * Inherit the resource settings, or apply the default behaviour of the
	 * {@link #strategy()} by default.
	 */
	String UNSPECIFIED_RESOURCE = "";

	/**
	 * This is an illegal key string due to the space, and so no accidental
	 * intersection with user defined keys or split strings should be possible.
	 */
	String UNSPECIFIED_KEY = "";

	/**
	 * This is an illegal key string due to the space, and so no accidental
	 * intersection with user defined keys or split strings should be possible.
	 */
	String UNSPECIFIED_KEY_SPLIT_STRING = "!UNSPECIFIED KEY SPLIT STRING!";

	/**
	 * A key formatting string describing the form
	 * {@code [class name].[method name]}.
	 */
	String UNQUALIFIED_DOTTED = "%2$s.%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [package name].[class name].[method name]}.
	 */
	String QUALIFIED_DOTTED = "%1$s.%2$s.%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [class name]::[method name]}.
	 */
	String UNQUALIFIED_SCOPED = "%2$s::%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [package name].[class name]::[method name]}.
	 */
	String QUALIFIED_SCOPED = "%1$s.%2$s::%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [class name]/[method name]}.
	 */
	String UNQUALIFIED_SLASHED = "%2$s/%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [package name]/[class name]/[method name]}.
	 */
	String QUALIFIED_SLASHED = "%1$s/%2$s/%3$s";

	/**
	 * @return the property key format string
	 */
	String key() default UNSPECIFIED_KEY;

	/**
	 * @return the split string to break up camel case when generating the key
	 *         arguments
	 */
	String keySplitString() default UNSPECIFIED_KEY;

	/**
	 * @return the treatment of character case when generating the key arguments
	 */
	KeyCase keyCase() default KeyCase.UNSPECIFIED;

	/**
	 * @return the level of requirement that property values are available
	 */
	Requirement requirement() default Requirement.UNSPECIFIED;

	/**
	 * @return the resource locations for a set of localisation strings, in
	 *         priority order
	 */
	String resource() default UNSPECIFIED_RESOURCE;

	/**
	 * The class of the {@link PropertyResourceBundle} which implements or
	 * specifies the strategy by which to resolve the localisation resources.
	 */
	Class<? extends PropertyResourceStrategy> strategy() default UnspecifiedPropertyResourceStrategy.class;
}
