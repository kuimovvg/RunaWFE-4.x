package org.jbpm.ui.util;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.widgets.Text;
import org.jbpm.ui.common.part.graph.NodeGraphicalEditPart;

public class LabelDirectEditManager extends DirectEditManager {

	public LabelDirectEditManager(GraphicalEditPart source, Class<?> editorType, CellEditorLocator locator) {
        super(source, editorType, locator);
    }

    @Override
    protected void initCellEditor() {
        Text text = (Text) getCellEditor().getControl();
        String name = ((NodeGraphicalEditPart) getEditPart()).getModel().getName();
        getCellEditor().setValue(name);
        text.selectAll();
    }

}
