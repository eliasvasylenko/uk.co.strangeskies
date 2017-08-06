package uk.co.strangeskies.reflection.codegen;

import java.util.Optional;

/*
 * TODO bad naming, sort it out
 */
public interface MethodImplementor {
  public <O, T> Optional<MethodImplementation<T>> getImplementation(
      MethodDeclaration<O, T> declaration);

}
