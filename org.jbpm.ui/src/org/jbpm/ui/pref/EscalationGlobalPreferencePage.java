package org.jbpm.ui.pref;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.common.model.Delegable;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.TimerAction;
import org.jbpm.ui.custom.CustomizationRegistry;
import org.jbpm.ui.custom.DelegableProvider;
import org.jbpm.ui.dialog.ChooseHandlerClassDialog;
import org.jbpm.ui.dialog.DurationEditDialog;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.TimerDuration;

public class EscalationGlobalPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {

    private final TimerAction timerAction;

    public EscalationGlobalPreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
        ProcessDefinition process = new ProcessDefinition();
        timerAction = new TimerAction(process);
        timerAction.setDelegationClassName("ru.runa.wf.EscalationActionHandler");
/*        {
            String string = DesignerPlugin.getPrefString(P_TASKS_TIMEOUT_ACTION_CLASS);
            if (string != null && !string.isEmpty()) {
                timerAction.setDelegationClassName(string);
            }
        }*/
        {
            String string = DesignerPlugin.getPrefString(P_ESCALATION_REPEAT);
            if (string != null && !string.isEmpty()) {
                timerAction.setRepeat(string);
            }
            string = DesignerPlugin.getPrefString(P_ESCALATION_CONFIG);
            if (string != null && !string.isEmpty()) {
                timerAction.setDelegationConfiguration(string);
            }
        }
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        addField(new DurationFieldEditor(P_ESCALATION_DURATION, Messages.getString("pref.escalation.duration"), getFieldEditorParent()));
        //addField(new ChooseHandlerClassFieldEditor(P_TASKS_TIMEOUT_ACTION_CLASS, Messages.getString("pref.escalation.actionClass"),
        //        getFieldEditorParent()));
        addField(new ConfigurationFieldEditor(P_ESCALATION_CONFIG, Messages.getString("pref.escalation.actionConfig"),
                getFieldEditorParent()));
        addField(new DurationFieldEditor(P_ESCALATION_REPEAT, Messages.getString("pref.escalation.repeat"), getFieldEditorParent()));
    }

    private class DurationFieldEditor extends StringButtonFieldEditor {

        private final TimerDuration editable;

        public DurationFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            getTextControl().setEditable(false);
            String duration = DesignerPlugin.getPrefString(name);
            editable = new TimerDuration(duration != null && !duration.isEmpty() ? duration : TimerDuration.EMPTY);
        }

        @Override
        protected String changePressed() {
            DurationEditDialog dialog = new DurationEditDialog(null, editable);
            TimerDuration timerDuration = (TimerDuration) dialog.openDialog();
            if (timerDuration != null) {
                editable.setDuration(timerDuration.getDuration());
            }
            if (!editable.hasDuration()) return "";
            return editable.getDuration();
        }

    }

    /*private class ChooseHandlerClassFieldEditor extends StringButtonFieldEditor {

        public ChooseHandlerClassFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            getTextControl().setEditable(false);
        }

        @Override
        protected String changePressed() {
            ChooseHandlerClassDialog dialog = new ChooseHandlerClassDialog(Delegable.ACTION_HANDLER);
            String string = dialog.openDialog();
            if (string != null && !string.isEmpty()) {
                timerAction.setDelegationClassName(string);
            }
            return string;
        }

    }*/

    private class ConfigurationFieldEditor extends StringButtonFieldEditor {

        public ConfigurationFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            getTextControl().setEditable(false);
        }

        @Override
        protected String changePressed() {
        	
            try {
                DelegableProvider provider = CustomizationRegistry.getProvider(timerAction.getDelegationClassName());
                String config = provider.showConfigurationDialog(timerAction);
                if (config != null) {
                    timerAction.setDelegationConfiguration(config);
                }
            } catch (Exception ex) {
                DesignerLogger.logError("Unable to open configuration dialog for " + timerAction.getDelegationClassName(), ex);
            }
            return timerAction.getDelegationConfiguration();
        }

    }

}
