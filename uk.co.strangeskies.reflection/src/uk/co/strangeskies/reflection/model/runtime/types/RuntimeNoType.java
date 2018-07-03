package uk.co.strangeskies.reflection.model.runtime.types;

import javax.lang.model.type.NoType;

/**
 * A specialization of {@link javax.lang.model.type.NoType} which operates over
 * the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeNoType extends RuntimeTypeMirror, NoType {}
