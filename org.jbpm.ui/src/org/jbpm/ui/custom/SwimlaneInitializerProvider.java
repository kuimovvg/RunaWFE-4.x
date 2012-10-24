package org.jbpm.ui.custom;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.jbpm.ui.common.model.Delegable;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.Swimlane;
import org.jbpm.ui.dialog.SwimlaneConfigDialog;

public class SwimlaneInitializerProvider extends DelegableProvider {

    @Override
    public String showConfigurationDialog(Delegable delegable) {
        Swimlane swimlane = (Swimlane) delegable;
        ProcessDefinition definition = swimlane.getProcessDefinition();

        String path = definition.getSwimlaneGUIConfiguration().getEditorPath(swimlane.getName());
        SwimlaneConfigDialog dialog = new SwimlaneConfigDialog(definition, swimlane, path);
        if (dialog.open() == IDialogConstants.OK_ID) {
            definition.getSwimlaneGUIConfiguration().putSwimlanePath(swimlane.getName(), dialog.getPath());
            return dialog.getConfiguration();
        }
        return null;
    }

}
