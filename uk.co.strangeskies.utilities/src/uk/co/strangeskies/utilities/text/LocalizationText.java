package uk.co.strangeskies.utilities.text;

import java.util.Locale;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.Self;

public interface LocalizationText<S extends LocalizationText<S>> extends Self<S>, Observable<S> {
	Locale getLocale();

	@Override
	default S copy() {
		return getThis();
	}
}
