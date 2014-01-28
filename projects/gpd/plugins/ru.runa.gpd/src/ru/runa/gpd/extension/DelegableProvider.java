package ru.runa.gpd.extension;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.osgi.framework.Bundle;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;

public class DelegableProvider {
    protected Bundle bundle;

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        return new DelegableConfigurationDialog(delegable.getDelegationConfiguration());
    }

    public String showConfigurationDialog(Delegable delegable) {
        DelegableConfigurationDialog dialog = createConfigurationDialog(delegable);
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    /**
     * Validates configuration. Implementors can return <code>false</code> to raise default invalid configuration message. Or can invoke delegable.addError. 
     * @return <code>false</code> for raising default invalid configuration message
     */
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) {
        return true;
    }
    
    /**
     * Callback is invoked when delegable is deleted from process definition.
     * @param delegable
     */
    public void onDelete(Delegable delegable) {
    }

    public List<Variable> getUsedVariables(Delegable delegable, ProcessDefinition processDefinition) {
        String configuration = delegable.getDelegationConfiguration();
        if (Strings.isNullOrEmpty(configuration)) {
            return Lists.newArrayList();
        }
        List<Variable> result = Lists.newArrayList();
        for (Variable variable : processDefinition.getVariables(true, true)) {
            if (configuration.contains(variable.getName())) {
                result.add(variable);
            }
        }
        return result;
    }
    
}
