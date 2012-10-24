package org.jbpm.ui.common.part.graph;

import org.eclipse.ui.IActionFilter;
import org.jbpm.ui.common.model.FormNode;

public class FormNodeEditPart extends SwimlaneNodeEditPart implements IActionFilter {

    @Override
    public FormNode getModel() {
        return (FormNode) super.getModel();
    }

	public boolean testAttribute(Object target, String name, String value) {
		if ("org.jbpm.ui.formExists".equals(name)) {
			return value.equals(String.valueOf(getModel().hasForm()));
		}
		return false;
	}

}
