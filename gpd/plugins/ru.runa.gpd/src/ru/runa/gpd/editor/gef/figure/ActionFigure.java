package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.gef.ActionGraphUtils;

public class ActionFigure extends NodeFigure {
    private boolean multiple = false;
    
    public static ActionFigure getMultipleFigure() {
        ActionFigure figure = new ActionFigure();
        figure.setMultiple(true);
        return figure;
    }

    public ActionFigure() {
        // TODO
        setSize(ActionGraphUtils.ACTION_SIZE+1, ActionGraphUtils.ACTION_SIZE+1);
        setPreferredSize(ActionGraphUtils.ACTION_SIZE+1, ActionGraphUtils.ACTION_SIZE+1);
    }
    
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }
    
    public boolean isMultiple() {
        return multiple;
    }
    
    @Override
    public void setLocation(Point p) {
        super.setLocation(p);
    }
    
    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        paintFigure(g, r);
    }
    
    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        paintFigure(g, r);
    }
    
    private void paintFigure(Graphics g, Rectangle r) {
        g.fillOval(r.x + 1, r.y + 1, ActionGraphUtils.ACTION_SIZE-2, ActionGraphUtils.ACTION_SIZE-2);
        g.drawOval(r.x + 1, r.y + 1, ActionGraphUtils.ACTION_SIZE-2, ActionGraphUtils.ACTION_SIZE-2);
        if (multiple) {
            //g.setLineWidth(2);
            g.drawLine(r.x+1, r.y+ActionGraphUtils.ACTION_SIZE/2, r.x+ActionGraphUtils.ACTION_SIZE-2, r.y+ActionGraphUtils.ACTION_SIZE/2);
            g.drawLine(r.x+ActionGraphUtils.ACTION_SIZE/2, r.y+1, r.x+ActionGraphUtils.ACTION_SIZE/2, r.y+ActionGraphUtils.ACTION_SIZE-2);
        }
    }
    
}
