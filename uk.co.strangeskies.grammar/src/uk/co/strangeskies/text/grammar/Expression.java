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

import static java.util.Arrays.asList;
import static uk.co.strangeskies.text.grammar.Symbol.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A production is something that can be produced by a symbol.
 * 
 * @author Elias N Vasylenko
 *
 */
public interface Expression {
  default Expression repeated() {
    return new Repetition(this);
  }

  default Expression repeated(int minimum) {
    List<Expression> symbols = new ArrayList<>(minimum + 1);
    symbols.add(new Repetition(this));
    for (int i = 0; i < minimum; i++) {
      symbols.add(this);
    }
    return new Concatenation(symbols);
  }

  default Expression except(Terminal<?> terminal) {
    // TODO Auto-generated method stub
    return null;
  }

  default Expression optionally() {
    return new Option(this);
  }

  default Expression or(Variable<Class<?>> rawType) {
    // TODO Auto-generated method stub
    return null;
  }

  default Expression then(Expression... append) {
    return then(asList(append));
  }

  default Expression then(Collection<? extends Expression> append) {
    List<Expression> symbols = new ArrayList<>(append.size() + 1);
    symbols.add(this);
    symbols.addAll(append);
    return new Concatenation(symbols);
  }

  default Expression then(String string) {
    return then(string(string));
  }
}
