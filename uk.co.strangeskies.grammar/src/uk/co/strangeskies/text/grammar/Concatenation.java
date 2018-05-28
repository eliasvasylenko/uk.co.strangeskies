package uk.co.strangeskies.text.grammar;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.stream.Stream;

public class Concatenation implements AnonymousSymbol {
  private final Production elements;

  public Concatenation(Expression... elements) {
    this(asList(elements));
  }

  public Concatenation(Collection<? extends Expression> elements) {
    this.elements = new Production(elements);
  }

  @Override
  public String toString() {
    return elements.getElements().map(Object::toString).collect(joining(" , "));
  }

  @Override
  public Stream<Production> produces() {
    return Stream.of(elements);
  }

  @Override
  public int hashCode() {
    return elements.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (!(obj instanceof Concatenation))
      return false;

    Concatenation that = (Concatenation) obj;

    return this.elements.equals(that.elements);
  }
}
