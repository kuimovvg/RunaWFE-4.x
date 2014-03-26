package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.MultiinstanceParameters;

public class MultiSubprocess extends Subprocess implements IMultiInstancesContainer {
    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        MultiinstanceParameters parameters = new MultiinstanceParameters(variableMappings);
        if (!parameters.isValid(true)) {
            errors.add(ValidationError.createLocalizedError(this, "multiinstance.noMultiinstanceLink"));
        }
    }

    @Override
    protected boolean isCompatibleVariables(Variable variable1, Variable variable2) {
        if (List.class.getName().equals(variable1.getJavaClassName())) {
            return true;
        }
        return super.isCompatibleVariables(variable1, variable2);
    }
}
