/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.observable.
 *
 * uk.co.strangeskies.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.observable;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import mockit.FullVerifications;
import mockit.FullVerificationsInOrder;
import mockit.Mocked;
import uk.co.strangeskies.observable.ObservableValue.Change;

@SuppressWarnings("javadoc")
public class ObservablePropertyImplTest {
  @Mocked
  Observer<String> downstreamObserver;
  @Mocked
  Observer<Change<String>> changeObserver;

  @Test
  public void getInitialValueTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    assertThat(property.get(), equalTo("initial"));
  }

  @Test
  public void getInitialValueMultipleTimesTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    assertThat(property.get(), equalTo("initial"));
    assertThat(property.get(), equalTo("initial"));
  }

  @Test
  public void initialValueMessageOnSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    property.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
      }
    };
  }

  @Test
  public void initialValueMessageOnSubscribeMultipleTimesTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    property.observe(downstreamObserver).cancel();
    property.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
      }
    };
  }

  @Test
  public void initialValueMessageThenCompleteTest() {
    ObservablePropertyImpl<String> property = new ObservablePropertyImpl<>("initial");

    property.observe(downstreamObserver);
    property.complete();

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
        downstreamObserver.onComplete();
      }
    };
  }

  @Test
  public void initialValueThenCompleteThenSubscribeTest() {
    ObservablePropertyImpl<String> property = new ObservablePropertyImpl<>("initial");

    property.complete();
    property.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
        downstreamObserver.onComplete();
      }
    };
  }

  @Test
  public void setValueMessageAfterSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    property.observe(downstreamObserver);
    property.set("message");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
        downstreamObserver.onNext("message");
      }
    };
  }

  @Test
  public void setValueMessageBeforeSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    property.set("message");
    property.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("message");
      }
    };
  }

  @Test
  public void setProblemEventAfterSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");
    Throwable problem = new Throwable();

    property.observe(downstreamObserver);
    property.setProblem(problem);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
        downstreamObserver.onFail(problem);
      }
    };
  }

  @Test
  public void setProblemEventBeforeSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");
    Throwable problem = new Throwable();

    property.setProblem(problem);
    property.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onFail(problem);
      }
    };
  }

  @Test(expected = MissingValueException.class)
  public void setProblemEventThenGetTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");
    Throwable problem = new Throwable();

    property.setProblem(problem);
    property.get();
  }

  @Test
  public void clearProblemEventThenGetTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");
    Throwable problem = new Throwable();

    property.setProblem(problem);
    property.set("message");
    assertThat(property.get(), equalTo("message"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void completeThenStartTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.complete();
    observable.start();
  }

  @Test(expected = NullPointerException.class)
  public void failWithNullThrowableTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.observe();
    observable.setProblem(null);
  }

  @Test(expected = NullPointerException.class)
  public void failWithNullMessageTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.observe();
    observable.set(null);
  }

  @Test
  public void noChangesOnObserveTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);

    new FullVerifications() {
      {
        changeObserver.onObserve((Observation) any);
      }
    };
  }

  @Test
  public void changeInitialToNextMessageTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);
    observable.set("message");

    new FullVerifications() {
      {
        changeObserver.onObserve((Observation) any);
        Change<String> change;
        changeObserver.onNext(change = withCapture());
        assertThat(change.previousValue(), equalTo("initial"));
        assertThat(change.newValue(), equalTo("message"));
      }
    };
  }

  @Test
  public void changeInitialToProblemTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);
    observable.setProblem(new Throwable());

    new FullVerifications() {
      {
        changeObserver.onObserve((Observation) any);
        Change<String> change;
        changeObserver.onNext(change = withCapture());
        assertThat(change.previousValue(), equalTo("initial"));
        assertFalse(change.tryNewValue().isPresent());
      }
    };
  }

  @Test
  public void changeProblemToNextMessageTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);
    observable.setProblem(new Throwable());
    observable.set("message");

    new FullVerifications() {
      {
        changeObserver.onObserve((Observation) any);
        Change<String> change;
        changeObserver.onNext(change = withCapture());
        assertFalse(change.tryPreviousValue().isPresent());
        assertThat(change.newValue(), equalTo("message"));
      }
    };
  }

  @Test
  public void changeProblemToProblemTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);
    observable.setProblem(new Throwable());
    observable.setProblem(new Throwable());

    new FullVerifications() {
      {
        Change<String> change;

        changeObserver.onObserve((Observation) any);

        changeObserver.onNext(change = withCapture());
        assertThat(change.previousValue(), equalTo("initial"));
        assertFalse(change.tryNewValue().isPresent());

        changeObserver.onNext(change = withCapture());
        assertFalse(change.tryPreviousValue().isPresent());
        assertFalse(change.tryNewValue().isPresent());
      }
    };
  }
}
