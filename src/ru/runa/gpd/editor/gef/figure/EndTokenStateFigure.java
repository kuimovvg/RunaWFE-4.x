package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.geometry.Rectangle;

public class EndTokenStateFigure extends TerminalFigure {
    @Override
    public void init(boolean bpmnNotation) {
        super.init(bpmnNotation);
        if (!bpmnNotation) {
            addEllipse();
            addLabel();
        }
    }

    @Override
    protected void addEllipse() {
        ellipse.setBounds(new Rectangle(3, 3, 16, 16));
        //ellipse.setBackgroundColor(ColorConstants.black);
        Ellipse outer = new Ellipse();
        outer.setSize(22, 22);
        outer.setOutline(false);
        outer.add(ellipse);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        gridData.horizontalSpan = 2;
        add(outer, gridData);
    }

    @Override
    protected void paintUMLFigure(Graphics g, Rectangle r) {
        g.translate(getLocation());
        int xShift = 20;//(r.width - 16) / 2;
        int yShift = 3;
        g.drawLine(xShift + 6, 6, xShift + 13, 13);
        g.drawLine(xShift + 13, 6, xShift + 6, 13);
    }

    @Override
    protected void paintBPMNFigure(Graphics g, Rectangle r) {
    }
}
