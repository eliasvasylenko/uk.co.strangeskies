package uk.co.strangeskies.text.grammar;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.text.grammar.Symbol.regex;
import static uk.co.strangeskies.text.grammar.Symbol.string;

import java.util.List;

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

  private final Variable<List<String>> sentence = new Variable<>("sentence");
  private final Variable<String> word = new Variable<>("word");
  private final Variable<Integer> character = new Variable<>("character");
  private final Variable<Integer> integer = new Variable<>("number");
  private final Variable<Integer> digit = new Variable<>("digit");
  private final Terminal<String> whitespace = regex("\\s+");
  private final Variable<Expression> expression = new Variable<>("expression");

  private void main() {
    Grammar mathematics = null;

    mathematics = mathematics
        .withRules(
            new RuleBuilder<>(expression)
                .matchingOutput(Addition.class)
                .producing(expression, whitespace, string("+"), whitespace, expression)
                .input(in -> new Addition(in.get(expression), in.get(expression)))
                .output(out -> t -> out.putAll(expression, t.getA(), t.getB())),

            new RuleBuilder<>(expression)
                .matchingOutput(Subtraction.class)
                .producing(expression, whitespace, string("-"), whitespace, expression)
                .input(in -> new Subtraction(in.get(expression), in.get(expression)))
                .output(out -> t -> out.putAll(expression, t.getA(), t.getB())),

            new RuleBuilder<>(expression)
                .matchingOutput(Multiplication.class)
                .producing(expression, whitespace, string("*"), whitespace, expression)
                .input(in -> new Multiplication(in.get(expression), in.get(expression)))
                .output(out -> t -> out.putAll(expression, t.getA(), t.getB())),

            new RuleBuilder<>(expression)
                .matchingOutput(Division.class)
                .producing(expression, whitespace, string("/"), whitespace, expression)
                .input(in -> new Division(in.get(expression), in.get(expression)))
                .output(out -> t -> out.putAll(expression, t.getA(), t.getB())),

            new RuleBuilder<>(sentence)
                .producing(word, whitespace.then(word).repeated())
                .input(in -> in.getAll(word).collect(toList()))
                .output(out -> t -> out.putAll(word, t)),

            new RuleBuilder<>(word)
                .producing(character.except(whitespace).repeated(1))
                .input(in -> new String(in.getAll(character).mapToInt(i -> i).toArray(), 0, 0))
                .output(out -> t -> t.codePoints().forEach(l -> out.put(character, l))));
  }

  public static void main(String... args) {
    new Scratchpad().main();
  }
}
