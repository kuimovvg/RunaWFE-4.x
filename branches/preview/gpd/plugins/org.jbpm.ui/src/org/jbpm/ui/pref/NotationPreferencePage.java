package ru.runa.bpm.ui.pref;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.resource.Messages;

public class NotationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public NotationPreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new RadioGroupFieldEditor(PrefConstants.P_DEFAULT_NOTATION, Messages.getString("pref.notation.defaultNotation"), 2, new String[][] {
                { "UML", "uml" }, { "BPMN", "bpmn" } }, getFieldEditorParent()));
    }

}
