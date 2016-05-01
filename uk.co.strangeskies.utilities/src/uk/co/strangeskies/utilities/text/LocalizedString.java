package uk.co.strangeskies.utilities.text;

import java.util.Locale;
import java.util.stream.IntStream;

import uk.co.strangeskies.utilities.Observable;

public interface LocalizedString extends Observable<String>, CharSequence {
	Locale locale();

	@Override
	String toString();

	@Override
	default int length() {
		return toString().length();
	}

	@Override
	default char charAt(int index) {
		return toString().charAt(index);
	}

	@Override
	default IntStream chars() {
		return toString().chars();
	}

	@Override
	default IntStream codePoints() {
		return toString().codePoints();
	}

	@Override
	default CharSequence subSequence(int start, int end) {
		return toString().subSequence(start, end);
	}
}
