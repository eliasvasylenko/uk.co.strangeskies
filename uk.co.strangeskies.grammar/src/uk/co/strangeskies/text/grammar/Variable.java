package uk.co.strangeskies.text.grammar;

public class Variable<T> implements Symbol<T> {
  private final String id;

  public Variable(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return '<' + id + '>';
  }
}
