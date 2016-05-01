package uk.co.strangeskies.utilities.text.test;

import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.LocalizedText;

@SuppressWarnings("javadoc")
public interface LocalizerTestText extends LocalizedText<LocalizerTestText> {
	LocalizedString missingMethod();

	LocalizedString simple();

	LocalizedString anotherSimple();

	LocalizedString substitution(String item);

	LocalizedString multipleSubstitution(String first, String second);

	default LocalizedString defaultMethod() {
		return substitution("default");
	}
}
