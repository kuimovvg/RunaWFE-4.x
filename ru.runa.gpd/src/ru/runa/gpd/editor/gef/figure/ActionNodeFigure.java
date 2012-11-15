package ru.runa.gpd.editor.gef.figure;


public class ActionNodeFigure extends StateFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        addLabel();
        addActionsContainer();
    }

}
