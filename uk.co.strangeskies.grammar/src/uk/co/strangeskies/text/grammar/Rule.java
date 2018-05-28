package uk.co.strangeskies.text.grammar;

public interface Rule {
  <T> Action<? extends T> getProduction(Variable<T> symbol);
}
