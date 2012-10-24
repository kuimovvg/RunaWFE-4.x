package org.jbpm.ui.jpdl3.figure;

import org.jbpm.ui.common.figure.StateFigure;

public class ActionNodeFigure extends StateFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        addLabel();
        addActionsContainer();
    }

}
