package org.jbpm.ui.common.part.tree;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.ui.JpdlVersionRegistry;
import org.jbpm.ui.common.ElementTypeDefinition;
import org.jbpm.ui.common.SwimlaneTypeDefinition;
import org.jbpm.ui.common.VariableTypeDefinition;
import org.jbpm.ui.common.model.EndState;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.GroupElement;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.StartState;

public class ProcessDefinitionTreeEditPart extends ElementTreeEditPart {

    @Override
    public ProcessDefinition getModel() {
        return (ProcessDefinition) super.getModel();
    }

    @Override
    protected List<GraphElement> getModelChildren() {
        List<GraphElement> result = new ArrayList<GraphElement>();
        result.addAll(getModel().getChildren(StartState.class));
        for (String categoryName : JpdlVersionRegistry.getPaletteCategories(getModel().getJpdlVersion())) {
            for (ElementTypeDefinition type : JpdlVersionRegistry.getPaletteEntriesFor(getModel().getJpdlVersion(), categoryName).values()) {
                if ("start-state".equals(type.getName()) || "end-state".equals(type.getName())) {
                    continue;
                }
                List<? extends GraphElement> elements = getModel().getChildren(type.getModelClass());
                if (elements.size() > 0) {
                    result.add(new GroupElement(getModel(), type));
                }
            }
        }
        result.add(new GroupElement(getModel(), new VariableTypeDefinition()));
        result.add(new GroupElement(getModel(), new SwimlaneTypeDefinition()));
        result.addAll(getModel().getChildren(EndState.class));
        return result;
    }

}
