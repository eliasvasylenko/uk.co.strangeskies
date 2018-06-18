package uk.co.strangeskies.text.grammar;

import java.util.function.Supplier;

public interface Context {
  <T> T get(Class<T> type);

  <T> T set(Class<T> type, T value);

  <T> T setIfAbsent(Class<T> type, Supplier<? extends T> value);
}
