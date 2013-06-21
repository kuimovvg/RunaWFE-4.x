package ru.runa.wfe.graph.image.figure;

import ru.runa.wfe.graph.image.model.NodeModel;
import ru.runa.wfe.graph.image.model.TransitionModel;

public abstract class AbstractFigureFactory {

    public abstract AbstractFigure createFigure(NodeModel nodeModel);

    public abstract TransitionFigureBase createTransitionFigure(TransitionModel transitionModel, AbstractFigure figureFrom, AbstractFigure figureTo);
}
