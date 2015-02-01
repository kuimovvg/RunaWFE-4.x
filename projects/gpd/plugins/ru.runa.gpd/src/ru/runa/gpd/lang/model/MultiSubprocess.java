package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.MultiinstanceParameters;
import ru.runa.gpd.util.VariableMapping;

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
    protected boolean isCompatibleVariables(VariableMapping mapping, Variable variable1, Variable variable2) {
        if (mapping.isMultiinstanceLink() && VariableFormatRegistry.isApplicable(variable1, List.class.getName())) {
            VariableFormatArtifact elementArtifact = VariableFormatRegistry.getInstance().getArtifact(variable1.getFormatComponentClassNames()[0]);
            return VariableFormatRegistry.isApplicable(variable2, elementArtifact.getJavaClassName());
        }
        return super.isCompatibleVariables(mapping, variable1, variable2);
    }
}
