package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.wizard.Wizard;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

public class VariableWizard extends Wizard {
    private final ProcessDefinition definition;
    private VariableNamePage namePage;
    private VariableFormatPage formatPage;
    private VariableDefaultValuePage defaultValuePage;
    private VariableAccessPage accessPage;
    private Variable variable;

    public VariableWizard(ProcessDefinition definition, Variable variable, boolean showNamePage, boolean editFormat) {
        this.definition = definition;
        if (showNamePage) {
            namePage = new VariableNamePage(definition, variable);
        }
        formatPage = new VariableFormatPage(variable, editFormat);
        defaultValuePage = new VariableDefaultValuePage(variable);
        accessPage = new VariableAccessPage(variable);
        setWindowTitle(showNamePage ? Localization.getString("VariableWizard.create") : Localization.getString("VariableWizard.edit"));
    }

    @Override
    public void addPages() {
        if (namePage != null) {
            addPage(namePage);
        }
        addPage(formatPage);
        addPage(defaultValuePage);
        addPage(accessPage);
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public boolean performFinish() {
        String name = null;
        String scriptingName = null;
        String description = null;
        if (namePage != null) {
            name = namePage.getVariableName();
            scriptingName = namePage.getScriptingVariableName();
            description = namePage.getVariableDesc();
        }
        String formatClassName = formatPage.getType().getName();
        String format = formatClassName;
        if (formatPage.getComponentClassNames().length != 0) {
            format += Variable.FORMAT_COMPONENT_TYPE_START;
            for (int i = 0; i < formatPage.getComponentClassNames().length; i++) {
                if (i != 0) {
                    format += Variable.FORMAT_COMPONENT_TYPE_CONCAT;
                }
                format += formatPage.getComponentClassNames()[i];
            }
            format += Variable.FORMAT_COMPONENT_TYPE_END;
        }
        String defaultValue = defaultValuePage.getDefaultValue();
        boolean publicVisibility = accessPage.isPublicVisibility();
        variable = new Variable(name, scriptingName, format, publicVisibility, defaultValue);
        variable.setDescription(description);
        return true;
    }
}
