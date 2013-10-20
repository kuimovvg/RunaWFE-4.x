package ru.runa.gpd.formeditor.ftl.view;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class TableLabelProvider extends BaseLabelProvider implements
		ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		/*ToolPalleteMethodTag todo = (ToolPalleteMethodTag) element;
		double zoom = 1d/2; //scale to half of the size maintaining aspect ratio 

		final int width = todo.getImage().getBounds().width; 
		final int height = todo.getImage().getBounds().height; 
		return new Image(Display.getDefault(),todo.getImage().getImageData().scaledTo((int)(width * zoom),(int)(height * zoom)));
		*/
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		ToolPalleteMethodTag todo = (ToolPalleteMethodTag) element;
		
		return todo.getTagLabel();
	}

}