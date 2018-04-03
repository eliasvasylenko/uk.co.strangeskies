package uk.co.strangeskies.text.grammar;

public class Symbol<T> {
  private final String id;

  public Symbol(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
