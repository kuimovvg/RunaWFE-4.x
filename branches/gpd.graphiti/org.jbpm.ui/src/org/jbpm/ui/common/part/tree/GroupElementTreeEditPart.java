package org.jbpm.ui.common.part.tree;

import java.util.List;

import org.jbpm.ui.SharedImages;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.GroupElement;

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
