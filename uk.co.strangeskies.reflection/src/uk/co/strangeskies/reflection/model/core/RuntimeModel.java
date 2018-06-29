package uk.co.strangeskies.reflection.model.core;

/**
 * TODO do we want separate instances of this class? There are certain areas
 * where caching is useful, but we probably don't want a global cache.
 */
public interface RuntimeModel {
  RuntimeTypes types();

  RuntimeElements elements();
}
