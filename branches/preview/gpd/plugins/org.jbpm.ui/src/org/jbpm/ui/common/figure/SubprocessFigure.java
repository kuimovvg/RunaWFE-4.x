package ru.runa.bpm.ui.common.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

public class SubprocessFigure extends StateFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        addLabel();
    }
    
    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        super.paintUMLFigure(g, r);
        paintSubprocessImage(g, getXShift(), 0);
    }
    
    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        super.paintBPMNFigure(g, r);
        //paintSubprocessImage(g, getXShift(), GRID_SIZE / 2);
        if (getClass() == SubprocessFigure.class) {
            int d = 2;
            int xCenter = getXShift() + r.width/2;
            int y = r.height - GRID_SIZE;
            g.drawRectangle(new Rectangle(xCenter - GRID_SIZE/2, y, GRID_SIZE, GRID_SIZE));
            g.drawLine(xCenter - GRID_SIZE/2 + d, y + GRID_SIZE/2, xCenter + GRID_SIZE/2 - d, y + GRID_SIZE/2);
            g.drawLine(xCenter, y + d, xCenter, y + GRID_SIZE - d);
        }
    }
    
    protected int getXShift() {
        return bpmnNotation ? GRID_SIZE / 2 : 0;
    }
    
    private void paintSubprocessImage(Graphics g, int xShift, int yShift) {
        Rectangle r = getBounds();
        g.drawLine(r.width - 20 - xShift, r.height - 10 - yShift, r.width - 10 - xShift, r.height - 10 - yShift);
        g.drawLine(r.width - 20 - xShift, r.height - 10 - yShift, r.width - 20 - xShift, r.height - 5 - yShift);
        g.drawLine(r.width - 15 - xShift, r.height - 15 - yShift, r.width - 15 - xShift, r.height - 5 - yShift);
        g.drawLine(r.width - 10 - xShift, r.height - 10 - yShift, r.width - 10 - xShift, r.height - 5 - yShift);
    }

}
