package org.jbpm.ui.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.resource.Messages;

public class TasksTimeoutPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public TasksTimeoutPreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        //addField(new BooleanFieldEditor(PrefConstants.P_TASKS_TIMEOUT_ENABLED, Messages.getString("pref.escalation.enabled"), getFieldEditorParent()));
    }

}
