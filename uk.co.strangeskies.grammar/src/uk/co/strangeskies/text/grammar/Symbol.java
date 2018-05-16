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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A production is something that can be produced by a symbol.
 * 
 * @author Elias N Vasylenko
 *
 */
public interface Symbol {
  public static Symbol empty() {
    // TODO Auto-generated method stub
    return null;
  }

  public static TerminalString string(String string) {
    // TODO Auto-generated method stub
    return null;
  }

  public static Terminal<String> regex(String string) {
    // TODO Auto-generated method stub
    return null;
  }

  default Symbol repeated() {
    return new Repetition(this);
  }

  default Symbol repeated(int minimum) {
    List<Symbol> symbols = new ArrayList<>(minimum + 1);
    symbols.add(new Repetition(this));
    for (int i = 0; i < minimum; i++) {
      symbols.add(this);
    }
    return new Concatenation(symbols);
  }

  default Symbol except(Terminal<?> terminal) {
    // TODO Auto-generated method stub
    return null;
  }

  default Symbol optionally() {
    return new Option(this);
  }

  default Symbol then(Symbol... append) {
    return then(asList(append));
  }

  default Symbol then(Collection<? extends Symbol> append) {
    List<Symbol> symbols = new ArrayList<>(append.size() + 1);
    symbols.add(this);
    symbols.addAll(append);
    return new Concatenation(symbols);
  }

  default Symbol then(String string) {
    return then(string(string));
  }
}
