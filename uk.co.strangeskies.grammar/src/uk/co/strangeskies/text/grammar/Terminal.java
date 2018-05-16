package uk.co.strangeskies.text.grammar;

public interface Terminal<T> extends Symbol {
  TerminalParser<T> parse(String input);

  TerminalComposer<T> compose(T output);
}
