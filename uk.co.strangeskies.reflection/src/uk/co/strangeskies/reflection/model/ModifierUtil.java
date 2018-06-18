package uk.co.strangeskies.reflection.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.lang.model.element.Modifier;

/*
 * Given an {@code int} value of modifiers, return a proper immutable set of
 * {@code Modifier}s as a result.
 */
class ModifierUtil {
  private ModifierUtil() {
    throw new AssertionError("No instances for you.");
  }

  // Exercise for the reader: explore if caching of sets of
  // Modifiers would be helpful.

  public static Set<Modifier> instance(int modifiers, boolean isDefault) {
    Set<Modifier> modSet = EnumSet.noneOf(Modifier.class);

    if (java.lang.reflect.Modifier.isAbstract(modifiers))
      modSet.add(Modifier.ABSTRACT);

    if (java.lang.reflect.Modifier.isFinal(modifiers))
      modSet.add(Modifier.FINAL);

    if (java.lang.reflect.Modifier.isNative(modifiers))
      modSet.add(Modifier.NATIVE);

    if (java.lang.reflect.Modifier.isPrivate(modifiers))
      modSet.add(Modifier.PRIVATE);

    if (java.lang.reflect.Modifier.isProtected(modifiers))
      modSet.add(Modifier.PROTECTED);

    if (java.lang.reflect.Modifier.isPublic(modifiers))
      modSet.add(Modifier.PUBLIC);

    if (java.lang.reflect.Modifier.isStatic(modifiers))
      modSet.add(Modifier.STATIC);

    if (java.lang.reflect.Modifier.isStrict(modifiers))
      modSet.add(Modifier.STRICTFP);

    if (java.lang.reflect.Modifier.isSynchronized(modifiers))
      modSet.add(Modifier.SYNCHRONIZED);

    if (java.lang.reflect.Modifier.isTransient(modifiers))
      modSet.add(Modifier.TRANSIENT);

    if (java.lang.reflect.Modifier.isVolatile(modifiers))
      modSet.add(Modifier.VOLATILE);

    if (isDefault)
      modSet.add(Modifier.DEFAULT);

    return Collections.unmodifiableSet(modSet);
  }
}