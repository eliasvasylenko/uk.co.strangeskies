package uk.co.strangeskies.reflection.model.runtime;

import uk.co.strangeskies.reflection.model.runtime.impl.RuntimeModelImpl;

/**
 * TODO do we want separate instances of this class? There are certain areas
 * where caching is useful, but we probably don't want a global cache.
 */
public interface RuntimeModel {
  static RuntimeModel instance(ClassLoader classLoader) {
    return new RuntimeModelImpl(classLoader);
  }

  ClassLoader getClassLoader();

  RuntimeTypes types();

  RuntimeElements elements();
}
