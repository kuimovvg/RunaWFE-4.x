package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class RenameUserTypeAttributeRefactoring extends Refactoring {
    private final List<VariableRenameProvider<?>> cache = new ArrayList<VariableRenameProvider<?>>();
    private final IFile definitionFile;
    private final ProcessDefinition processDefinition;
    private final VariableUserType type;
    private final Variable oldAttribute;
    private final String newAttributeName;
    private final String newAttributeScriptingName;
    private final List<Refactoring> refactorings = Lists.newArrayList();

    public RenameUserTypeAttributeRefactoring(IFile definitionFile, ProcessDefinition processDefinition, VariableUserType type,
            Variable oldAttribute, String newAttributeName, String newAttributeScriptingName) {
        this.definitionFile = definitionFile;
        this.processDefinition = processDefinition;
        this.type = type;
        this.oldAttribute = oldAttribute;
        this.newAttributeName = newAttributeName;
        this.newAttributeScriptingName = newAttributeScriptingName;
    }

    private void searchInVariables(List<Variable> result, VariableUserType searchType, Variable searchAttribute, Variable parent,
            List<Variable> children) {
        for (Variable variable : children) {
            if (variable.getUserType() == null) {
                continue;
            }
            String syntheticName = (parent != null ? (parent.getName() + VariableUserType.DELIM) : "") + variable.getName();
            String syntheticScriptingName = (parent != null ? (parent.getScriptingName() + VariableUserType.DELIM) : "")
                    + variable.getScriptingName();
            if (Objects.equal(variable.getUserType(), searchType)) {
                Variable syntheticVariable = new Variable(syntheticName + VariableUserType.DELIM + searchAttribute.getName(), syntheticScriptingName
                        + VariableUserType.DELIM + searchAttribute.getScriptingName(), variable);
                result.add(syntheticVariable);
            } else {
                Variable syntheticVariable = new Variable(syntheticName, syntheticScriptingName, variable);
                searchInVariables(result, searchType, searchAttribute, syntheticVariable, variable.getUserType().getAttributes());
            }
        }
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor progressMonitor) {
        RefactoringStatus status = new RefactoringStatus();
        List<Variable> result = Lists.newArrayList();
        searchInVariables(result, type, oldAttribute, null, processDefinition.getVariables(false, false));
        for (Variable variable : result) {
            String newName = variable.getName().replace(VariableUserType.DELIM + oldAttribute.getName(), VariableUserType.DELIM + newAttributeName);
            String newScriptingName = variable.getScriptingName().replace(VariableUserType.DELIM + oldAttribute.getScriptingName(),
                    VariableUserType.DELIM + newAttributeScriptingName);
            RenameVariableRefactoring refactoring = new RenameVariableRefactoring(definitionFile, processDefinition, variable, newName,
                    newScriptingName);
            status.merge(refactoring.checkInitialConditions(progressMonitor));
            refactorings.add(refactoring);
        }
        return status;
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) {
        return new RefactoringStatus();
    }

    @Override
    public CompositeChange createChange(IProgressMonitor progressMonitor) throws CoreException {
        CompositeChange compositeChange = new CompositeChange(getName());
        for (Refactoring refactoring : refactorings) {
            compositeChange.merge((CompositeChange) refactoring.createChange(progressMonitor));
        }
        return compositeChange;
    }

    public boolean isUserInteractionNeeded() {
        try {
            checkInitialConditions(null);
            return createChange(null).getChildren().length > 0;
        } catch (CoreException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getName() {
        return processDefinition.getName();
    }
}
