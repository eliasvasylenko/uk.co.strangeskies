package uk.co.strangeskies.text.grammar;

import static java.util.function.Function.identity;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RuleBuilder<T> {
  private final Variable<T> variable;

  public RuleBuilder(Variable<T> variable) {
    this.variable = variable;
  }

  public <U> RuleBuilder<U> transforming(
      Function<? super T, ? extends U> transformationOut,
      Function<? super U, ? extends T> transformationIn) {
	  return null; // TODO
  }

  public <U extends T> RuleBuilder<U> matchingOutput(Class<U> type, Predicate<? super U> matcher) {
    return matchingOutput(type).matchingOutput(matcher);
  }

  public <U extends T> RuleBuilder<U> matchingOutput(Class<U> type) {
	return null;
}

  public RuleBuilder<T> matchingOutput(Predicate<? super T> matcher) {
	return null;
}

  public ProductionRuleInputBuilder<T> producing(Expression... productions) {
	return null;
}

  public <U> SingleProductionRuleInputBuilder<T, U> producingSymbol(Symbol<U> production) {
	return null;
}

  public Rule producingIdentity(Symbol<T> production) {
    return producingSymbol(production).input(identity()).output(identity());
  }

  public interface ProductionRuleInputBuilder<T> {
    ProductionRuleOutputBuilder<T> input(Function<? super SymbolsIn, ? extends T> in);
  }

  public interface ProductionRuleOutputBuilder<T> {
    Rule output(Function<? super SymbolsOut, ? extends Consumer<? super T>> out);
  }

  public interface SingleProductionRuleInputBuilder<T, U> {
    SingleProductionRuleOutputBuilder<T, U> input(Function<? super U, ? extends T> in);
  }

  public interface SingleProductionRuleOutputBuilder<T, U> {
    Rule output(Function<? super T, ? extends U> out);
  }
}
