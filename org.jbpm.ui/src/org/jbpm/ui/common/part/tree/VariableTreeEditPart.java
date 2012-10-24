package org.jbpm.ui.common.part.tree;

import org.jbpm.ui.common.model.Variable;

public class VariableTreeEditPart extends ElementTreeEditPart {

    public VariableTreeEditPart(Variable variable) {
        setModel(variable);
    }

    @Override
    public Variable getModel() {
        return (Variable) super.getModel();
    }

}
