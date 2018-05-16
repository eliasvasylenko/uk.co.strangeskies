package uk.co.strangeskies.text.grammar;

public interface Rule<T> {
  Variable<? super T> getSymbol();

  Production getProduction();
}
