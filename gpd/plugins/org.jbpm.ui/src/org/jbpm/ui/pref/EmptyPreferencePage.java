package ru.runa.bpm.ui.pref;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EmptyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    @Override
    protected void createFieldEditors() {
    }

    @Override
    public void init(IWorkbench workbench) {
    }

}
