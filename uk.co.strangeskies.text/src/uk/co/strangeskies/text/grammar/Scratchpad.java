package uk.co.strangeskies.text.grammar;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static uk.co.strangeskies.text.grammar.Production.produce;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scratchpad {
  interface Expression {}

  class Addition implements Expression {
    public Addition(Expression a, Expression b) {
      // TODO Auto-generated constructor stub
    }

    Expression getA() {
      return null;
    }

    Expression getB() {
      return null;
    }
  }

  class Subtraction implements Expression {
    public Subtraction(Expression a, Expression b) {
      // TODO Auto-generated constructor stub
    }

    Expression getA() {
      return null;
    }

    Expression getB() {
      return null;
    }
  }

  class Multiplication implements Expression {
    public Multiplication(Expression a, Expression b) {
      // TODO Auto-generated constructor stub
    }

    Expression getA() {
      return null;
    }

    Expression getB() {
      return null;
    }
  }

  class Division implements Expression {
    public Division(Expression a, Expression b) {
      // TODO Auto-generated constructor stub
    }

    Expression getA() {
      return null;
    }

    Expression getB() {
      return null;
    }
  }

  private final Symbol<List<String>> sentence = new Symbol<>("sentence");
  private final Symbol<String> word = new Symbol<>("word");
  private final Symbol<Integer> letter = new Symbol<>("letter");
  private final Symbol<Integer> integer = new Symbol<>("number");
  private final Symbol<Integer> digit = new Symbol<>("digit");
  private final Symbol<Void> whitespace = new Symbol<>("whitespace");
  private final Symbol<Void> optionalWhitespace = new Symbol<>("optional-whitespace");
  private final Symbol<Expression> expression = new Symbol<>("expression");

  private void main() {
    Grammar mathematics = new Grammar();

    mathematics
        .addRule(
            expression,
            produce(expression)
                .append("+")
                .append(expression, Addition::new, Addition::getA, Addition::getB));
    mathematics
        .addRule(
            expression,
            produce(expression)
                .append("-")
                .append(expression, Subtraction::new, Subtraction::getA, Subtraction::getB));
    mathematics
        .addRule(
            expression,
            produce(expression)
                .append("*")
                .append(
                    expression,
                    Multiplication::new,
                    Multiplication::getA,
                    Multiplication::getB));
    mathematics
        .addRule(
            expression,
            produce(expression)
                .append("/")
                .append(expression, Division::new, Division::getA, Division::getB));

    mathematics
        .addRule(
            sentence,
            produce(word)
                .append(
                    produce(word).prepend(whitespace).repeat(),
                    (h, t) -> concat(of(h), t),
                    s -> s.findFirst().get(),
                    s -> s.skip(1))
                .map(s -> s.collect(toList()), null));
  }

  public static void main(String... args) {
    new Scratchpad().main();
  }
}
