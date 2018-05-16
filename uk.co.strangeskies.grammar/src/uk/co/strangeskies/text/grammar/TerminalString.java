package uk.co.strangeskies.text.grammar;

public class TerminalString implements Terminal<String> {
  private final String string;

  protected TerminalString(String string) {
    this.string = string;
  }

  @Override
  public TerminalParser<String> parse(String input) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TerminalComposer<String> compose(String output) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    return '"' + string + '"';
  }
}
