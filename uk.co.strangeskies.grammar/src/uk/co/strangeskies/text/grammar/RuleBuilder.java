package uk.co.strangeskies.text.grammar;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface RuleBuilder<T> {
  static <T> RuleBuilder<T> buildRuleFor(Variable<T> symbol) {
    return null;// TODO
  }

  <U extends T> RuleBuilder<U> matchingOutput(Class<U> type, Predicate<? super U> matcher);

  <U extends T> RuleBuilder<U> matchingOutput(Class<U> type);

  RuleBuilder<T> matchingOutput(Predicate<? super T> matcher);

  ProductionRuleInputBuilder<T> producing(Symbol... productions);

  interface ProductionRuleInputBuilder<T> {
    ProductionRuleOutputBuilder<T> input(Function<? super SymbolsIn, ? extends T> in);
  }

  interface ProductionRuleOutputBuilder<T> {
    Rule<T> output(Function<? super SymbolsOut, ? extends Consumer<? super T>> out);
  }
}
