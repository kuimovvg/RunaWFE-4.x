package ru.runa.bpm.ui.common.figure.bpmn;

import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.PointList;

public class DefaultDecisionFlowDecoration extends PolylineDecoration {
    public static final PointList LINE_TIP = new PointList();
    static {
        LINE_TIP.addPoint(-3, 1);
        LINE_TIP.addPoint(-5, -1);
    }
    
    public DefaultDecisionFlowDecoration() {
        setTemplate(LINE_TIP);
        setScale(4, 4);
        setLineWidth(2);
    }

}
