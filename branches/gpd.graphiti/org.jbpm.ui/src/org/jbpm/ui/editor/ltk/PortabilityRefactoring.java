package org.jbpm.ui.editor.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.JpdlVersionRegistry;
import org.jbpm.ui.common.ElementTypeDefinition;
import org.jbpm.ui.common.model.Action;
import org.jbpm.ui.common.model.Decision;
import org.jbpm.ui.common.model.FormNode;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.State;
import org.jbpm.ui.common.model.Subprocess;
import org.jbpm.ui.common.model.WaitState;
import org.jbpm.ui.resource.Messages;

public class PortabilityRefactoring extends Refactoring {

    private final List<VariableRenameProvider<?>> cache = new ArrayList<VariableRenameProvider<?>>();
    private final IFile definitionFile;
    private final ProcessDefinition definition;
    private final String variableName;
    private final String replacement;

    public PortabilityRefactoring(IFile definitionFile, ProcessDefinition definition, String variableName, String replacement) {
        this.definitionFile = definitionFile;
        this.definition = definition;
        this.variableName = variableName;
        this.replacement = replacement;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) {
        RefactoringStatus result = new RefactoringStatus();
        try {
            if (cache.size() == 0) {
                IFolder folder = (IFolder) definitionFile.getParent();

                List<FormNode> formNodes = definition.getChildren(FormNode.class);
                for (FormNode formNode : formNodes) {
                    cache.add(new FormNodePresentation(folder, formNode));
                }

                List<State> stateNodes = definition.getChildren(State.class);
                for (State stateNode : stateNodes) {
                    cache.add(new TimedPresentation(stateNode));
                }
                List<WaitState> waitStateNodes = definition.getChildren(WaitState.class);
                for (WaitState waitStateNode : waitStateNodes) {
                    cache.add(new TimedPresentation(waitStateNode));
                }

                List<Action> actions = definition.getChildrenRecursive(Action.class);
                for (Action action : actions) {
                    cache.add(new DelegablePresentation(action, action.getDisplayName()));
                }
                List<Decision> decisions = definition.getChildren(Decision.class);
                for (Decision decision : decisions) {
                    cache.add(new DelegablePresentation(decision, decision.getName()));
                }
                List<Subprocess> subprocesses = definition.getChildren(Subprocess.class);
                for (Subprocess subprocess : subprocesses) {
                    cache.add(new SubprocessPresentation(subprocess));
                }
                List<ElementTypeDefinition> typesWithProvider = JpdlVersionRegistry.getTypesWithVariableRenameProvider(definition.getJpdlVersion());
                for (ElementTypeDefinition elementTypeDefinition : typesWithProvider) {
                    List<? extends GraphElement> list = definition.getChildren(elementTypeDefinition.getModelClass());
                    for (GraphElement graphElement : list) {
                        VariableRenameProvider provider = elementTypeDefinition.createVariableRenameProvider();
                        provider.setElement(graphElement);
                        cache.add(provider);
                    }
                }
            }
        } catch (Exception e) {
            DesignerLogger.logErrorWithoutDialog(e.getMessage(), e);
            result.addFatalError(Messages.getString("UnhandledException"));
        }
        return result;
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) {
        return new RefactoringStatus();
    }

    private CompositeChange cashedChange = null;

    @Override
    public CompositeChange createChange(IProgressMonitor pm) {
        if (cashedChange == null) {
            cashedChange = new CompositeChange(getName());
            for (VariableRenameProvider<?> classPresentation : cache) {
                try {
                    List<Change> changes = classPresentation.getChanges(variableName, replacement);
                    cashedChange.addAll(changes.toArray(new Change[changes.size()]));
                } catch (Exception e) {
                    DesignerLogger.logErrorWithoutDialog(e.getMessage(), e);
                }
            }
        }
        return cashedChange;
    }

    public boolean isUserInteractionNeeded() {
        checkInitialConditions(null);
        return createChange(null).getChildren().length > 0;
    }

    @Override
    public String getName() {
        return definition.getName();
    }

}
