package ru.runa.bpm.ui.common.figure.uml;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import ru.runa.bpm.ui.common.figure.GEFConstants;
import ru.runa.bpm.ui.common.figure.TimerStateFigure;

public class TimerAnchor extends AbstractConnectionAnchor implements GEFConstants {
    boolean bpmn;
	
    public TimerAnchor(TimerStateFigure owner) {
        super(owner);
        bpmn = owner.isBpmnNotation();
    }

    @Override
    public Point getReferencePoint() {
        Rectangle bounds = getOwner().getBounds().getCopy();
        getOwner().translateToAbsolute(bounds);
        //if (bpmn)
        //    return new Point(bounds.x + 3*GRID_SIZE/2, bounds.y + bounds.height - 3*GRID_SIZE/2);
        return new Point(bounds.x + GRID_SIZE, bounds.y + bounds.height - GRID_SIZE);
    }
    
    public Point getLocation(Point reference) {
        Rectangle bounds = getOwner().getBounds().getCopy();
        getOwner().translateToAbsolute(bounds);

        Point center = getReferencePoint();
        Point ref = center.getCopy().negate().translate(reference);
        if (ref.x == 0)
            return new Point(reference.x, (ref.y > 0) ? bounds.bottom() : bounds.bottom() - 2 * GRID_SIZE);
        if (ref.y == 0)
            return new Point((ref.x > 0) ? bounds.x + 2 * GRID_SIZE : bounds.x, reference.y);

        float dx = (ref.x > 0) ? 0.5f : -0.5f;
        float dy = (ref.y > 0) ? 0.5f : -0.5f;

        float k = (float) (ref.y * 2 * GRID_SIZE) / (ref.x * 2 * GRID_SIZE);
        k = k * k;

        return center.translate((int) (2 * GRID_SIZE * dx / Math.sqrt(1 + k)), (int) (2 * GRID_SIZE * dy / Math.sqrt(1 + 1 / k)));
    }
}
