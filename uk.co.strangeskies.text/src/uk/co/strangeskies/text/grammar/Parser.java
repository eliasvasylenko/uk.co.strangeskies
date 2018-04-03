package uk.co.strangeskies.text.grammar;

public interface Parser {
  <T> T parse(Symbol<T> symbol, String string);

  <T> String compose(Symbol<T> symbol, T object);
}
