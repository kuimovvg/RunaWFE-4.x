package ru.runa.bpm.ui.common.part.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import ru.runa.bpm.ui.common.model.ProcessDefinition;

public class OutlineRootTreeEditPart extends AbstractTreeEditPart {
    private final List<ProcessDefinition> modelChildren = new ArrayList<ProcessDefinition>();

    @Override
    public void setModel(Object model) {
        super.setModel(model);
        modelChildren.add((ProcessDefinition) model);
    }

    @Override
    protected List<ProcessDefinition> getModelChildren() {
        return modelChildren;
    }

    @Override
    public void refresh() {
        super.refresh();
        refreshChildren();
        refreshVisuals();
    }

}
