package ru.runa.bpm.ui.jpdl3.figure;

import ru.runa.bpm.ui.common.figure.StateFigure;

public class ActionNodeFigure extends StateFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        addLabel();
        addActionsContainer();
    }

}
