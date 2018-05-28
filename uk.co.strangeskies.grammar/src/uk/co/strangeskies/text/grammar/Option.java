package uk.co.strangeskies.text.grammar;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class Option implements AnonymousSymbol {
  private final Production accept;
  private final Production decline;

  public Option(Expression... elements) {
    this(asList(elements));
  }

  public Option(Collection<? extends Expression> elements) {
    this.accept = new Production(elements);
    this.decline = new Production(Symbol.empty());
  }

  @Override
  public Stream<Production> produces() {
    return Stream.of(accept, decline);
  }

  @Override
  public String toString() {
    return "{ " + accept.getElements().map(Object::toString).collect(joining(", ")) + " }";
  }

  @Override
  public int hashCode() {
    return Objects.hash(accept);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (!(obj instanceof Option))
      return false;

    Option that = (Option) obj;

    return this.accept.equals(that.accept);
  }
}
