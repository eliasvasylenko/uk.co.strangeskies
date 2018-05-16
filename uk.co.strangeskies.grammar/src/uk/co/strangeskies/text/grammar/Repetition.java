package uk.co.strangeskies.text.grammar;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.stream.Stream;

public class Repetition implements AnonymousSymbol {
  private final Symbol element;
  private final Production termination;
  private final Production continuation;

  public Repetition(Symbol element) {
    this.element = requireNonNull(element);
    this.termination = new Production(Symbol.empty());
    this.continuation = new Production(this, element);
  }

  @Override
  public Stream<Production> produces() {
    return Stream.of(termination, continuation);
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
    if (!(obj instanceof Repetition))
      return false;

    Repetition that = (Repetition) obj;

    return this.element.equals(that.element);
  }
}
