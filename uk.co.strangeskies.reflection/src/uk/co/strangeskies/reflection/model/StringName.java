package uk.co.strangeskies.reflection.model;

import java.util.Objects;

import javax.lang.model.element.Name;

public class StringName implements Name {
  private String name;

  private StringName(String name) {
    this.name = Objects.requireNonNull(name);
  }

  public static StringName instance(String name) {
    return new StringName(name);
  }

  @Override
  public int length() {
    return name.length();
  }

  @Override
  public char charAt(int index) {
    return name.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return name.subSequence(start, end);
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof StringName) {
      return name.equals(((StringName) other).name);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean contentEquals(CharSequence cs) {
    return name.contentEquals(cs);
  }
}