package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.mm.Property;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.GaProperty;

import com.google.common.base.Objects;

public abstract class UpdateFeature extends AbstractUpdateFeature {
    private DiagramFeatureProvider featureProvider;

    public UpdateFeature() {
        super(null);
    }

    public void setFeatureProvider(DiagramFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public DiagramFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public boolean canUpdate(IUpdateContext context) {
        return true;
    }

    protected String findTextValueRecursive(PictogramElement pe, String name) {
        GraphicsAlgorithm ga = findGaRecursiveByName(pe, name);
        if (ga instanceof Text) {
            return ((Text) ga).getValue();
        }
        if (ga instanceof MultiText) {
            return ((MultiText) ga).getValue();
        }
        return null;
    }

    protected GraphicsAlgorithm findGaRecursiveByName(PictogramElement pe, String name) {
        GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
        for (Property property : pe.getProperties()) {
            if (Objects.equal(GaProperty.ID, property.getKey()) && Objects.equal(name, property.getValue())) {
                return ga;
            }
        }
        GraphicsAlgorithm result = findGaRecursiveByName(ga, name);
        if (result != null) {
            return result;
        }
        if (pe instanceof Connection) {
            Connection connection = (Connection) pe;
            for (ConnectionDecorator connectionDecorator : connection.getConnectionDecorators()) {
                result = findGaRecursiveByName(connectionDecorator, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    protected GraphicsAlgorithm findGaRecursiveByName(GraphicsAlgorithm ga, String name) {
        for (Property property : ga.getProperties()) {
            if (Objects.equal(GaProperty.ID, property.getKey()) && Objects.equal(name, property.getValue())) {
                return ga;
            }
        }
        for (GraphicsAlgorithm childGa : ga.getGraphicsAlgorithmChildren()) {
            GraphicsAlgorithm result = findGaRecursiveByName(childGa, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    protected void setProperty(PictogramElement pe, String name, String value) {
        GraphicsAlgorithm ga = findGaRecursiveByName(pe, name);
        if (ga instanceof Text) {
            ((Text) ga).setValue(value);
        }
        if (ga instanceof MultiText) {
            ((MultiText) ga).setValue(value);
        }
    }
}
