package ru.runa.gpd.extension.assign;

import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.ui.dialog.SwimlaneConfigDialog;

public class SwimlaneInitializerProvider extends DelegableProvider {

    @Override
    public String showConfigurationDialog(Delegable delegable) {
        if (!HandlerArtifact.ASSIGNMENT.equals(delegable.getDelegationType())) {
            throw new IllegalArgumentException("For assignment handler only");
        }
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
