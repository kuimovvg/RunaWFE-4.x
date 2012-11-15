package ru.runa.gpd.settings;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;

public class NotationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public NotationPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new RadioGroupFieldEditor(PrefConstants.P_DEFAULT_NOTATION, Localization.getString("pref.notation.defaultNotation"), 2, new String[][] {
                { "UML", "uml" }, { "BPMN", "bpmn" } }, getFieldEditorParent()));
    }

}
