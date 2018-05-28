/*
 * Copyright (C) 2018 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.grammar;

import static java.util.stream.Collectors.toList;

import java.util.List;

/**
 * A production is something that can be produced by a symbol.
 * 
 * @author Elias N Vasylenko
 *
 */
public class ListVariable<T> extends Variable<List<T>> {
  private final Variable<T> element;

  public ListVariable(String id, Variable<T> element) {
    super(id);
    this.element = element;
  }

  public Variable<T> getElement() {
    return element;
  }

  public void a() {
    new Rule() {
      @SuppressWarnings("unchecked")
      @Override
      public <E> Action<E> getProduction(Variable<E> symbol) {
        if (symbol instanceof ListVariable<?>)
          return (Action<E>) getListProduction((ListVariable<?>) symbol);
        else
          return null;
      }

      private <E> Action<List<E>> getListProduction(ListVariable<E> symbol) {
        Variable<E> element = symbol.getElement();

        return new Action<List<E>>(
            Symbol.string("["),
            element.then(Symbol.string(",").then(element).repeated()).optionally(),
            Symbol.string("]")) {
          @Override
          public List<E> input(SymbolsIn symbols) {
            return symbols.getAll(element).collect(toList());
          }

          @Override
          public boolean output(SymbolsOut symbols, List<E> out) {
            symbols.putAll(element, out);
            return true;
          }
        };
      }
    };
  }
}
