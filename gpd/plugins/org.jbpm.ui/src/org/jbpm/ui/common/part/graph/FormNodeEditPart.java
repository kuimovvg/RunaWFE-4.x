package ru.runa.bpm.ui.common.part.graph;

import org.eclipse.ui.IActionFilter;
import ru.runa.bpm.ui.common.model.FormNode;

public class FormNodeEditPart extends SwimlaneNodeEditPart implements IActionFilter {

    @Override
    public FormNode getModel() {
        return (FormNode) super.getModel();
    }

	public boolean testAttribute(Object target, String name, String value) {
		if ("ru.runa.bpm.ui.formExists".equals(name)) {
			return value.equals(String.valueOf(getModel().hasForm()));
		}
		return false;
	}

}
