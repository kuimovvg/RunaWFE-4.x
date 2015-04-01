package ru.runa.wfe.graph.history.figure;

import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.history.model.NodeModel;
import ru.runa.wfe.graph.history.model.TransitionModel;

public abstract class AbstractFigureFactory {

    public abstract AbstractFigure createFigure(NodeModel nodeModel, boolean useEgdingOnly, RenderHits renderHits);

    public abstract TransitionFigureBase createTransitionFigure(TransitionModel transitionModel, AbstractFigure figureFrom, AbstractFigure figureTo,
            RenderHits renderHits);
}
