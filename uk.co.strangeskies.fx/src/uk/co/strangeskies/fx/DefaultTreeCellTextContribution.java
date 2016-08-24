package uk.co.strangeskies.fx;

public class DefaultTreeCellTextContribution implements TreeCellContribution<Object>, TreeTextContribution<Object> {
	@Override
	public String getText(Object data) {
		return data.toString();
	}

	@Override
	public String getSupplementalText(Object data) {
		return null;
	}

	@Override
	public void configureCell(Object data, String text, String supplementalText, TreeCellImpl cell) {
		cell.setGraphic(cell.defaultGraphic());
		TreeTextContribution.super.configureCell(data, text, supplementalText, cell);
	}
}
