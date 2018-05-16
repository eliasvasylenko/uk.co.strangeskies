package uk.co.strangeskies.text.properties;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface Key {
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
   * @return the property key format string
   */
  String value();
}
