package ru.runa.bpm.ui.common.part.tree;

import ru.runa.bpm.ui.common.model.Variable;

public class VariableTreeEditPart extends ElementTreeEditPart {

    public VariableTreeEditPart(Variable variable) {
        setModel(variable);
    }

    @Override
    public Variable getModel() {
        return (Variable) super.getModel();
    }

}
