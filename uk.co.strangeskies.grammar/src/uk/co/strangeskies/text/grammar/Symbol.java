package uk.co.strangeskies.text.grammar;

public interface Symbol<T> extends Expression {
  public static Symbol<Void> empty() {
    // TODO Auto-generated method stub
    return null;
  }

  public static TerminalString string(String string) {
    // TODO Auto-generated method stub
    return null;
  }

  public static Terminal<String> regex(String string) {
    // TODO Auto-generated method stub
    return null;
  }
}
