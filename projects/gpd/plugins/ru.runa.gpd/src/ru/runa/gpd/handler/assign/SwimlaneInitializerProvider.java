package ru.runa.gpd.handler.assign;

import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.handler.DelegableProvider;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;

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
