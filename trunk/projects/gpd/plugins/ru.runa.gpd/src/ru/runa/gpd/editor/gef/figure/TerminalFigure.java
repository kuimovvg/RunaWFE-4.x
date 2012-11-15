package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

public abstract class TerminalFigure extends NodeFigure {

    protected final Ellipse ellipse = new Ellipse();

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        if (bpmnNotation) {
            ellipse.setSize(48, 48);
            ellipse.setVisible(false);
            add(ellipse);
        }
        connectionAnchor = new CircleAnchor(ellipse);
    }

    @Override
    public boolean isResizeable() {
        return !bpmnNotation;
    }

    protected abstract void addEllipse();

    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
    }

}
