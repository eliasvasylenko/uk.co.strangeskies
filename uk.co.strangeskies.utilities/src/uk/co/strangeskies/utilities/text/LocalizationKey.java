package uk.co.strangeskies.utilities.text;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for instance methods of sub-interfaces of {@link LocalizedText} to
 * specify property key for resource delegation.
 * 
 * @author Elias N Vasylenko
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface LocalizationKey {
	/**
	 * @return the resource key to source the annotated method string
	 */
	String value();
}
