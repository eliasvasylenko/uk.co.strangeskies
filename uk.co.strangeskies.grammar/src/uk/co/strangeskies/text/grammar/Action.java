package uk.co.strangeskies.text.grammar;

import java.util.Collection;

public abstract class Action<T> extends Production {
  public Action(Expression... entries) {
    super(entries);
  }

  public Action(Collection<? extends Expression> entries) {
    super(entries);
  }

  public abstract T input(SymbolsIn symbols);

  public abstract boolean output(SymbolsOut symbols, T out);
}
