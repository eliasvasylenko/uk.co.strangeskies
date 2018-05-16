package uk.co.strangeskies.text.grammar;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class Production {
  private final List<Symbol> entries;

  public Production(Symbol... entries) {
    this(asList(entries));
  }

  public Production(Collection<? extends Symbol> entries) {
    this.entries = new ArrayList<>(entries);
  }

  @Override
  public String toString() {
    return entries.stream().map(Object::toString).collect(joining(", "));
  }

  public Stream<Symbol> getElements() {
    return entries.stream();
  }

  @Override
  public int hashCode() {
    return entries.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (!(obj instanceof Production))
      return false;

    Production that = (Production) obj;

    return entries.equals(that.entries);
  }
}
