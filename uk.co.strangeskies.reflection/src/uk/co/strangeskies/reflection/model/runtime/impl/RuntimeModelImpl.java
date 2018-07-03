package uk.co.strangeskies.reflection.model.runtime.impl;

import uk.co.strangeskies.reflection.model.runtime.RuntimeModel;

public class RuntimeModelImpl implements RuntimeModel {
  private final ClassLoader classLoader;
  private final RuntimeTypesImpl types;
  private final RuntimeElementsImpl elements;

  public RuntimeModelImpl(ClassLoader classLoader) {
    this.classLoader = classLoader;
    this.types = new RuntimeTypesImpl(this);
    this.elements = new RuntimeElementsImpl(this);
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public RuntimeTypesImpl types() {
    return types;
  }

  @Override
  public RuntimeElementsImpl elements() {
    return elements;
  }
}
