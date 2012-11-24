package ru.runa.gpd.settings;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.handler.DelegableProvider;
import ru.runa.gpd.handler.HandlerRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.TimerDuration;
import ru.runa.wfe.handler.action.EscalationActionHandler;

public class EscalationGlobalPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private final TimerAction timerAction;

    public EscalationGlobalPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        ProcessDefinition process = new ProcessDefinition(); // TODO remove this!
        timerAction = new TimerAction(process);
        timerAction.setDelegationClassName(EscalationActionHandler.class.getName());
        {
            String string = Activator.getPrefString(P_ESCALATION_REPEAT);
            if (string != null && !string.isEmpty()) {
                timerAction.setRepeat(string);
            }
            string = Activator.getPrefString(P_ESCALATION_CONFIG);
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
        addField(new DurationFieldEditor(P_ESCALATION_DURATION, Localization.getString("pref.escalation.duration"), getFieldEditorParent()));
        //addField(new ChooseHandlerClassFieldEditor(P_TASKS_TIMEOUT_ACTION_CLASS, Messages.getString("pref.escalation.actionClass"),
        //        getFieldEditorParent()));
        addField(new ConfigurationFieldEditor(P_ESCALATION_CONFIG, Localization.getString("pref.escalation.actionConfig"), getFieldEditorParent()));
        addField(new DurationFieldEditor(P_ESCALATION_REPEAT, Localization.getString("pref.escalation.repeat"), getFieldEditorParent()));
    }

    private class DurationFieldEditor extends StringButtonFieldEditor {
        private final TimerDuration editable;

        public DurationFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            getTextControl().setEditable(false);
            String duration = Activator.getPrefString(name);
            editable = new TimerDuration(duration != null && !duration.isEmpty() ? duration : TimerDuration.EMPTY);
        }

        @Override
        protected String changePressed() {
            DurationEditDialog dialog = new DurationEditDialog(null, editable);
            TimerDuration timerDuration = (TimerDuration) dialog.openDialog();
            if (timerDuration != null) {
                editable.setDuration(timerDuration.getDuration());
            }
            if (!editable.hasDuration()) {
                return "";
            }
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
                DelegableProvider provider = HandlerRegistry.getProvider(timerAction.getDelegationClassName());
                String config = provider.showConfigurationDialog(timerAction);
                if (config != null) {
                    timerAction.setDelegationConfiguration(config);
                }
            } catch (Exception ex) {
                PluginLogger.logError("Unable to open configuration dialog for " + timerAction.getDelegationClassName(), ex);
            }
            return timerAction.getDelegationConfiguration();
        }
    }
}
