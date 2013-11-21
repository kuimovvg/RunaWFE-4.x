package ru.runa.wfe.graph.history.figure;

import ru.runa.wfe.graph.history.model.NodeModel;
import ru.runa.wfe.graph.history.model.TransitionModel;

public abstract class AbstractFigureFactory {

    public abstract AbstractFigure createFigure(NodeModel nodeModel, boolean useEgdingOnly);

    public abstract TransitionFigureBase createTransitionFigure(TransitionModel transitionModel, AbstractFigure figureFrom, AbstractFigure figureTo);
}
