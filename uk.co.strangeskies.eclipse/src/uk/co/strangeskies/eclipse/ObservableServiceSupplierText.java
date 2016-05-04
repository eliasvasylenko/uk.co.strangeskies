package uk.co.strangeskies.eclipse;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.LocalizedText;

/**
 * Text resource accessor for Eclipse OSGi utilities
 * 
 * @author Elias N Vasylenko
 */
public interface ObservableServiceSupplierText extends LocalizedText<ObservableServiceSupplierText> {
	/**
	 * @return invalid type was annotated with {@link ObservableService} for
	 *         service collection injection
	 */
	default LocalizedString illegalInjectionTarget() {
		return illegalInjectionTarget(ObservableService.class, ObservableList.class, ObservableSet.class,
				ObservableValue.class);
	}

	/**
	 * @param observableService
	 *          the {@link ObservableService} class for service collection
	 *          injection
	 * @param list
	 *          an observable list in service ranking order
	 * @param set
	 *          an observable set in service ranking order
	 * @param value
	 *          an observable value of the highest ranking service
	 * @return invalid type was annotated with {@link ObservableService} for
	 *         service collection injection
	 */
	@SuppressWarnings("rawtypes")
	LocalizedString illegalInjectionTarget(Class<ObservableService> observableService, Class<ObservableList> list,
			Class<ObservableSet> set, Class<ObservableValue> value);

	/**
	 * @return an unexpected error occurred
	 */
	LocalizedString unexpectedError();
}
