package uk.co.strangeskies.text.grammar;

import java.util.Objects;
import java.util.stream.Stream;

public class Option implements AnonymousSymbol {
  private final Symbol element;
  private final Production accept;
  private final Production decline;

  public Option(Symbol element) {
    this.element = Objects.requireNonNull(element);
    this.accept = new Production(element);
    this.decline = new Production(Symbol.empty());
  }

  @Override
  public Stream<Production> produces() {
    return Stream.of(accept, decline);
  }

  @Override
  public String toString() {
    return "{ " + element + " }";
  }

  public Symbol getElement() {
    return element;
  }

  @Override
  public int hashCode() {
    return Objects.hash(element);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (!(obj instanceof Option))
      return false;

    Option that = (Option) obj;

    return this.element.equals(that.element);
  }
}
