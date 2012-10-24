package org.jbpm.ui.common.figure.uml;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;

public interface ReferencedConnectionAnchor extends ConnectionAnchor {

    public Point getReferencePoint(Point reference);
    
}
