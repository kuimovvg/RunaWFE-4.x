package org.jbpm.ui.common.figure;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.common.figure.uml.TimerAnchor;
import org.jbpm.ui.pref.PrefConstants;

public class TimerStateFigure extends StateFigure {
    private boolean timerExist = false;
    private boolean minimizedView = false;

    private ConnectionAnchor timerConnectionAnchor;

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        boolean hideLabel = bpmnNotation && !DesignerPlugin.getPrefBoolean(PrefConstants.P_BPMN_SHOW_SWIMLANE);
        if (!hideLabel) {
            addSwimlaneLabel();
        }
        addLabel();
        addActionsContainer();
        timerConnectionAnchor = new TimerAnchor(this);
    }

    @Override
    public Dimension getDefaultSize() {
        return DIM_RECTANGLE.getExpanded(GRID_SIZE, GRID_SIZE);
    }

    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        Rectangle borderRect = r.getResized(-1, -1);
        if (!minimizedView) {
            g.drawRoundRectangle(borderRect, 20, 10);
            g.translate(getBounds().getLocation());
            paintTimer(g, 0);
        } else {
            g.drawRectangle(borderRect);
        }
    }

    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        if (!minimizedView) {
            super.paintBPMNFigure(g, r);
            paintTimer(g, GRID_SIZE / 2);
        } else {
            Rectangle borderRect = r.getResized(11, 11).translate(-5, -5);
            g.drawRectangle(borderRect);
        }
    }

    private void paintTimer(Graphics g, int shift) {
        if (timerExist) {
            Rectangle r = getBounds();
            g.fillOval(shift, r.height - GRID_SIZE * 2 - shift, GRID_SIZE * 2, GRID_SIZE * 2);
            g.drawOval(shift + 1, 1 + r.height - GRID_SIZE * 2 - shift, GRID_SIZE * 2 - 2, GRID_SIZE * 2 - 2);
            g.drawLine(shift + GRID_SIZE, r.height - GRID_SIZE - shift, shift + GRID_SIZE, r.height - GRID_SIZE + 5 - shift);
            g.drawLine(shift + GRID_SIZE, r.height - GRID_SIZE - shift, shift + GRID_SIZE + 5, r.height - GRID_SIZE - 5 - shift);
        }
    }

    private Rectangle getFrameArea(Rectangle origin) {
        if (minimizedView) {
            return new Rectangle(origin.x + GRID_SIZE/2, origin.y + GRID_SIZE/2, origin.width - GRID_SIZE, origin.height - GRID_SIZE);
        } else {
            return new Rectangle(origin.x + GRID_SIZE, origin.y, origin.width - GRID_SIZE, origin.height - GRID_SIZE);
        }
    }

    @Override
    public Rectangle getClientArea(Rectangle rect) {
        Rectangle r = super.getClientArea(rect);
        return getFrameArea(r);
    }

    @Override
    protected Rectangle getBox() {
        Rectangle r = getBounds().getCopy();
        return getFrameArea(r);
    }

    public ConnectionAnchor getTimerConnectionAnchor() {
        return timerConnectionAnchor;
    }

    public void setTimerExist(boolean timerExist) {
        this.timerExist = timerExist;
        repaint();
    }

    @Override
    public void setBounds(Rectangle rect) {
        int minimizedSize = 2 * GEFConstants.GRID_SIZE;
        if (minimizedView) {
            rect.width = minimizedSize;
            rect.height = minimizedSize;
        }
        if (!minimizedView && rect.width == minimizedSize) {
            rect.width = getDefaultSize().width;
            rect.height = getDefaultSize().height;
        }
        super.setBounds(rect);
    }
    
    public void setMinimizedView(boolean minimazedView, boolean showActions) {
        this.minimizedView = minimazedView;
        if (this.minimizedView) {
            label.setVisible(false);
            if (swimlaneLabel != null) {
                swimlaneLabel.setVisible(false);
            }
            actionsContainer.setVisible(false);
        } else {
            label.setVisible(true);
            if (swimlaneLabel != null) {
                swimlaneLabel.setVisible(true);
            }
            actionsContainer.setVisible(showActions);
        }
    }

}
