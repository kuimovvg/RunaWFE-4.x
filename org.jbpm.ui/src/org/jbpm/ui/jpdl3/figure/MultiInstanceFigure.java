package org.jbpm.ui.jpdl3.figure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.jbpm.ui.common.figure.SubprocessFigure;

public class MultiInstanceFigure extends SubprocessFigure {
    
    @Override
    public void setBounds(Rectangle rect) {
        if (rect.height < 4*GRID_SIZE) {
            rect.height = 4*GRID_SIZE;
        }
        super.setBounds(rect);
    }
    
    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        super.paintUMLFigure(g, r);
        paintSurroudingBoxes(g, 0);
    }
    
    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        super.paintBPMNFigure(g, r);
        //paintSurroudingBoxes(g, GRID_SIZE / 2);
        int w = 4;
        int dw = 2;
        int xCenter = getXShift() + r.width/2;
        int y = r.height - GRID_SIZE;
        List<Rectangle> rects = new ArrayList<Rectangle>();
        rects.add(new Rectangle(xCenter - 3*w/2 - dw, y, w, GRID_SIZE));
        rects.add(new Rectangle(xCenter - w/2, y, w, GRID_SIZE));
        rects.add(new Rectangle(xCenter + w/2 + dw, y, w, GRID_SIZE));
        Color bgColor = g.getBackgroundColor();
        g.setBackgroundColor(g.getForegroundColor());
        for (Rectangle rectangle : rects) {
            g.fillRectangle(rectangle);
        }
        g.setBackgroundColor(bgColor);
    }
    
    @Override
    protected int getXShift() {
        //return bpmnNotation ? GRID_SIZE : GRID_SIZE / 2;
        return GRID_SIZE / 2;
    }
    
    private void paintSurroudingBoxes(Graphics g, int shift) {
        Rectangle b = getBounds();
        List<Rectangle> rects = new ArrayList<Rectangle>();
        rects.add(new Rectangle(shift, b.height/2-3*GRID_SIZE/2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(shift, b.height/2-GRID_SIZE/2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(shift, b.height/2+GRID_SIZE/2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(b.width-GRID_SIZE-1-shift, b.height/2-3*GRID_SIZE/2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(b.width-GRID_SIZE-1-shift, b.height/2-GRID_SIZE/2, GRID_SIZE, GRID_SIZE));
        rects.add(new Rectangle(b.width-GRID_SIZE-1-shift, b.height/2+GRID_SIZE/2, GRID_SIZE, GRID_SIZE));
        for (Rectangle rectangle : rects) {
            g.fillRectangle(rectangle);
            g.drawRectangle(rectangle);
        }
    }

    @Override
    public Rectangle getClientArea(Rectangle rect) {
        Rectangle r = super.getClientArea(rect);
        Rectangle borderRect = r.getCopy();
        if (!bpmnNotation) {
            borderRect.expand(-GRID_SIZE / 2, 0);
        }
        return borderRect;
    }

}
