package ru.runa.bpm.ui.common.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.geometry.Rectangle;
import ru.runa.bpm.ui.SharedImages;

public class StartStateFigure extends TerminalFigure {

    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        if (!bpmnNotation) {
            addSwimlaneLabel();
            addLabel();
            addEllipse();
        }
    }

    @Override
    protected void addEllipse() {
        ellipse.setBounds(new Rectangle(3, 3, 16, 16));
        ellipse.setBackgroundColor(ColorConstants.black);

        Ellipse outer = new Ellipse();
        outer.setSize(22, 22);
        outer.setOutline(false);
        outer.add(ellipse);

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        gridData.horizontalSpan = 2;
        add(outer, gridData);
    }

    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
        g.drawImage(SharedImages.getImage("icons/bpmn/graph/start.png"), r.getLocation());
    }

    @Override
    public ConnectionAnchor getArrivingConnectionAnchor() {
        return null;
    }

}
