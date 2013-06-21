package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.gef.figure.uml.ForkJoinConnectionAnchor;

public class ForkJoinFigure extends NodeFigure {
    @Override
    public void init() {
        super.init();
        connectionAnchor = new ForkJoinConnectionAnchor(this);
    }

    @Override
    public Dimension getDefaultSize() {
        return DIM_SLIM.getCopy();
    }

    @Override
    public void setBounds(Rectangle rect) {
        if (rect.width < rect.height) {
            rect.width = 5;
        } else {
            rect.height = 5;
        }
        super.setBounds(rect);
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        g.setBackgroundColor(ColorConstants.black);
        g.fillRectangle(0, 0, dim.width, dim.height);
    }
}
