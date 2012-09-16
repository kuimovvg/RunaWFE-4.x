package ru.runa.bpm.ui.common.part.tree;

import java.util.List;

import ru.runa.bpm.ui.SharedImages;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.GroupElement;

public class GroupElementTreeEditPart extends ElementTreeEditPart {

    public GroupElementTreeEditPart(GroupElement element) {
        setModel(element);
    }

    @Override
    public GroupElement getModel() {
        return (GroupElement) super.getModel();
    }

    @Override
    protected List<? extends GraphElement> getModelChildren() {
        return getModel().getProcessDefinition().getChildren(getModel().getType().getModelClass());
    }

    @Override
    protected void refreshVisuals() {
        setWidgetImage(SharedImages.getImage("icons/obj/group.gif"));
        setWidgetText(getModel().getType().getEntryLabel());
    }

}
