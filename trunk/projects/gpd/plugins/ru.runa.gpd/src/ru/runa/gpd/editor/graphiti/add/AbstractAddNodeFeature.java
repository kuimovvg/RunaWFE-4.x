package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Subprocess;

public abstract class AbstractAddNodeFeature extends AbstractAddShapeFeature implements GEFConstants {
    public AbstractAddNodeFeature(IFeatureProvider provider) {
        super(provider);
    }

    public boolean isFixedSize() {
        return false;
    }

    @Override
    public boolean canAdd(IAddContext context) {
        if (context.getNewObject() instanceof Node) {
            Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
            if (context.getTargetContainer() instanceof Diagram || parentObject instanceof Subprocess) {
                return true;
            }
        }
        return false;
    }
}
