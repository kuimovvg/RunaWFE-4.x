package org.jbpm.ui.jpdl3.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.jbpm.ui.common.figure.CircleAnchor;
import org.jbpm.ui.common.figure.NodeFigure;

public abstract class MessageNodeFigure extends NodeFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        if (!bpmnNotation) {
            addLabel();
        }
        if (bpmnNotation) {
            connectionAnchor = new CircleAnchor(this);
        }
    }

    // TODO: update on size change
    protected void addEmptySpace(int position, int width) {
        Figure figure = (Figure) label.getParent().getParent();
        ((GridData) getLayoutManager().getConstraint(figure)).horizontalSpan = 1;
        GridData data = new GridData(GridData.FILL_VERTICAL);
        data.widthHint = width;
        add(new Figure(), data, position);
    }

    @Override
    public boolean isResizeable() {
        return !bpmnNotation;
    }

}
