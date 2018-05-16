package uk.co.strangeskies.text.grammar;

import java.util.stream.Stream;

public interface AnonymousSymbol extends Symbol {
  Stream<Production> produces();
}
