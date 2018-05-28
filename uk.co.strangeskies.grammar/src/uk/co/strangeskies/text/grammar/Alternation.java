package uk.co.strangeskies.text.grammar;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Alternation implements AnonymousSymbol {
  private final Set<Expression> elements;
  private final List<Production> options;

  public Alternation(Collection<? extends Expression> elements) {
    this.elements = new LinkedHashSet<>(elements);
    this.options = elements.stream().map(Production::new).collect(toList());
  }

  @Override
  public String toString() {
    return elements.stream().map(Object::toString).collect(joining(" | "));
  }

  @Override
  public Stream<Production> produces() {
    return options.stream();
  }

  @Override
  public int hashCode() {
    return options.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (!(obj instanceof Alternation))
      return false;

    Alternation that = (Alternation) obj;

    return new ArrayList<>(options).equals(new ArrayList<>(that.options));
  }
}
