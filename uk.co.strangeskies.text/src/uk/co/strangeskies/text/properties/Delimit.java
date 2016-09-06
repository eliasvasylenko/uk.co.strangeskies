package uk.co.strangeskies.text.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * An annotation for {@link PropertyLoader properties} of types such as
 * {@link List} to specify how elements should be delimited by the parser.
 * 
 * @author Elias N Vasylenko
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
public @interface Delimit {
	/**
	 * @return the delimiter string
	 */
	String value() default ",";

	/**
	 * Whether list element delimiting is eager or lazy. And eager strategy will
	 * split the string by the delimiter before parsing the elements, whereas a
	 * lazy strategy will attempt to parse an element over the entire input
	 * string, then look for the delimiter after a match is found.
	 * 
	 * @return true for an eager delimiting strategy, false otherwise
	 */
	boolean eager() default true;

	/**
	 * @return true to trim whitespace between elements, false otherwise
	 */
	boolean ignoreWhitespace() default true;
}
