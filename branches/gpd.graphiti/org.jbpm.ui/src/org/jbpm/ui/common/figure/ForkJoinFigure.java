package org.jbpm.ui.common.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.jbpm.ui.SharedImages;
import org.jbpm.ui.common.figure.uml.ForkJoinConnectionAnchor;

public class ForkJoinFigure extends NodeFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        if (bpmnNotation) {
            connectionAnchor = new DiamondAnchor(this);
        } else {
            connectionAnchor = new ForkJoinConnectionAnchor(this);
        }
    }
    
    @Override
    public boolean isResizeable() {
        return !bpmnNotation;
    }
    
    @Override
    public Dimension getDefaultSize() {
        if (!bpmnNotation)
            return DIM_SLIM.getCopy();
        return DIM_SQUARE.getCopy();
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
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        g.drawImage(SharedImages.getImage("icons/bpmn/graph/fork_join.png"), r.getLocation());
    }
    
    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        g.translate(r.getLocation());
        g.setBackgroundColor(ColorConstants.black);
        g.fillRectangle(0, 0, r.width, r.height);
    }
    
}
