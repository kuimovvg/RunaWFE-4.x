package ru.runa.gpd.ui.graphiti.edit;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.impl.AbstractDirectEditingFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.jbpm.ui.common.model.Node;

public class DirectEditNodeFeature extends AbstractDirectEditingFeature {
    private boolean isMultiLine = false;

    public DirectEditNodeFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public int getEditingType() {
        if (isMultiLine) {
            return TYPE_MULTILINETEXT;
        } else {
            return TYPE_TEXT;
        }
    }

    @Override
    public boolean canDirectEdit(IDirectEditingContext context) {
        PictogramElement pe = context.getPictogramElement();
        Object bo = getBusinessObjectForPictogramElement(pe);
        GraphicsAlgorithm ga = context.getGraphicsAlgorithm();
        if (bo instanceof Node && ga instanceof MultiText) {
            isMultiLine = true;
            return true;
        } else if (bo instanceof Node && ga instanceof Text) {
            isMultiLine = false;
            return true;
        }
        // direct editing not supported in all other cases
        return false;
    }

    @Override
    public String getInitialValue(IDirectEditingContext context) {
        // return the current name of the EClass
        PictogramElement pe = context.getPictogramElement();
        Node flowElement = (Node) getBusinessObjectForPictogramElement(pe);
        return flowElement.getName();
    }

    @Override
    public String checkValueValid(String value, IDirectEditingContext context) {
        if (value.length() < 1) {
            return "Please enter any text."; //$NON-NLS-1$
        }
        if (isMultiLine == false && value.contains("\n")) {
            return "Line breakes are not allowed."; //$NON-NLS-1$
        }
        // null means, that the value is valid
        return null;
    }

    @Override
    public void setValue(String value, IDirectEditingContext context) {
        // set the new name for the EClass
        PictogramElement pe = context.getPictogramElement();
        Node flowElement = (Node) getBusinessObjectForPictogramElement(pe);
        flowElement.setName(value);
        // Explicitly update the shape to display the new value in the diagram
        // Note, that this might not be necessary in future versions of the GFW
        // (currently in discussion)
        // we know, that pe is the Shape of the Text, so its container is the
        // main shape of the EClass
        updatePictogramElement(((Shape) pe).getContainer());
    }
}
