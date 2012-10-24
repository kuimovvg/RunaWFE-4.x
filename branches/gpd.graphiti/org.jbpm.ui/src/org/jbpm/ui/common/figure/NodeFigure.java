package org.jbpm.ui.common.figure;

import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.PageFlowLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.common.model.Swimlane;

public abstract class NodeFigure extends Figure implements GEFConstants {

    protected static final Dimension DIM_RECTANGLE = new Dimension(10 * GRID_SIZE, 6 * GRID_SIZE);
    protected static final Dimension DIM_SQUARE = new Dimension(4 * GRID_SIZE, 4 * GRID_SIZE);
    protected static final Dimension DIM_SLIM = new Dimension(16 * GRID_SIZE, 5);

    protected static final Color veryLightBlue = new Color(null, 246, 247, 255);
    protected static final Color lightBlue = new Color(null, 3, 104, 154);

    protected TextFlow swimlaneLabel;
    protected TextFlow label;
    protected ActionsContainer actionsContainer;

    protected ConnectionAnchor connectionAnchor = null;

    protected boolean bpmnNotation = false;

    public void init(boolean bpmnNotation) {
        this.bpmnNotation = bpmnNotation;
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        setLayoutManager(layout);
    }

    public ActionsContainer getActionsContainer() {
        return actionsContainer;
    }

    public ConnectionAnchor getLeavingConnectionAnchor() {
        return connectionAnchor;
    }

    public ConnectionAnchor getArrivingConnectionAnchor() {
        return connectionAnchor;
    }

    public TextFlow getLabel() {
        return label;
    }

    public void setName(String name) {
        if (label != null) {
            label.setText(name);
        }
    }

    public Dimension getDefaultSize() {
        if (bpmnNotation) {
            return DIM_SQUARE.getCopy();
        }
        return DIM_RECTANGLE.getCopy();
    }

    @Override
    public void setBounds(Rectangle rect) {
        if (!isResizeable() || rect.width == 0 || rect.height == 0) {
            rect.setSize(getDefaultSize());
        }
        super.setBounds(rect);
    }

    protected Rectangle getBox() {
        return getBounds();
    }

    protected void addSwimlaneLabel() {
        swimlaneLabel = new TextFlow();
        FlowPage fp = new FlowPage();
        fp.setHorizontalAligment(PositionConstants.CENTER);
        fp.add(swimlaneLabel);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        add(fp, gridData);
    }

    protected void addLabel() {
        Figure figure = new Figure();
        CenteredFlowLayout layout = new CenteredFlowLayout();
        layout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
        figure.setLayoutManager(layout);
        figure.setOpaque(false);

        label = new TextFlow();

        FlowPage fp = new FlowPage();
        fp.setLayoutManager(new PageFlowLayout(fp));
        fp.setHorizontalAligment(PositionConstants.CENTER);
        fp.add(label);

        figure.add(fp, FlowLayout.ALIGN_CENTER);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        add(figure, gridData);
    }

    protected void addActionsContainer() {
        actionsContainer = new ActionsContainer();
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 2;
        add(actionsContainer, gridData);
    }

    @Override
    public void add(IFigure figure, Object constraint, int index) {
        if (figure instanceof ActionFigure) {
            actionsContainer.addSafely(figure, constraint, index);
        } else {
            super.add(figure, constraint, index);
        }
    }

    @Override
    public void remove(IFigure figure) {
        if (figure instanceof ActionFigure) {
            actionsContainer.removeSafely(figure);
        } else {
            super.remove(figure);
        }
    }

    // Find action (which doesn't have layout constraint)
    @SuppressWarnings("unchecked")
    @Override
    public IFigure findFigureAt(int x, int y, TreeSearch search) {
        for (IFigure figure : (List<IFigure>) getChildren()) {
            if (figure instanceof ActionFigure && figure.getBounds().contains(x, y)) {
                return figure;
            }
        }
        return super.findFigureAt(x, y, search);
    }

    public void setSwimlaneName(Swimlane swimlane) {
        if (swimlaneLabel != null) {
            swimlaneLabel.setText(swimlane != null ? "(" + swimlane.getName() + ")" : "");
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (!DesignerPlugin.getDefault().getDialogSettings().getBoolean(PluginConstants.DISABLE_ANTIALIASING)) {
            graphics.setTextAntialias(SWT.ON);
            graphics.setAntialias(SWT.ON);
        }
        super.paint(graphics);
    }

    @Override
    protected final void paintFigure(Graphics graphics) {
        Rectangle r = getClientArea().getCopy();
        if (bpmnNotation) {
            graphics.setLineWidth(2);
            Color foregroundColor = graphics.getForegroundColor();
            Color backgroundColor = graphics.getBackgroundColor();
            graphics.setBackgroundColor(veryLightBlue);
            graphics.setForegroundColor(lightBlue);
            paintBPMNFigure(graphics, r);
            graphics.setBackgroundColor(backgroundColor);
            graphics.setForegroundColor(foregroundColor);
        } else {
            paintUMLFigure(graphics, r);
        }
    }

    public boolean isResizeable() {
        return true;
    }

    public boolean isBpmnNotation() {
        return bpmnNotation;
    }

    protected abstract void paintBPMNFigure(Graphics g, Rectangle r);

    protected abstract void paintUMLFigure(Graphics g, Rectangle r);
}
