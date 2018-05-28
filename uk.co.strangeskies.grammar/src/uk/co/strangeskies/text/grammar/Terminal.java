package uk.co.strangeskies.text.grammar;

public interface Terminal<T> extends Symbol<T> {
  TerminalParser<T> parse(String input);

  TerminalComposer<T> compose(T output);
}
