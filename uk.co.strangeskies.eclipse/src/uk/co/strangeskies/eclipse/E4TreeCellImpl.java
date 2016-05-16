package uk.co.strangeskies.eclipse;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.fx.core.di.LocalInstance;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TreeItem;
import uk.co.strangeskies.fx.TreeCellImpl;

/**
 * A basic tree cell implementation for {@link TreeItem} for e(fx)clipse
 * applications.
 * 
 * @author Elias N Vasylenko
 */
@Creatable
public class E4TreeCellImpl extends TreeCellImpl {
	/**
	 * @param loader
	 *          the FXML loader given by e(fx)clipse
	 */
	@Inject
	public E4TreeCellImpl(@LocalInstance FXMLLoader loader) {
		super(loader);
	}
}
