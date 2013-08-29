package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.BotTaskType;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.State;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;

@SuppressWarnings("unchecked")
public class PortabilityRefactoring extends Refactoring {
    private final List<VariableRenameProvider<?>> cache = new ArrayList<VariableRenameProvider<?>>();
    private final IFile definitionFile;
    private final ProcessDefinition definition;
    private final Variable oldVariable;
    private final Variable newVariable;

    public PortabilityRefactoring(IFile definitionFile, ProcessDefinition definition, Variable oldVariable, Variable newVariable) {
        this.definitionFile = definitionFile;
        this.definition = definition;
        this.oldVariable = oldVariable;
        this.newVariable = newVariable;
    }

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
                List<Timer> timers = definition.getChildren(Timer.class);
                for (Timer timer : timers) {
                    cache.add(new TimerPresentation(timer));
                }
                List<TaskState> taskStates = definition.getChildren(TaskState.class);
                for (TaskState taskState : taskStates) {
                    if (taskState.getBotTaskLink() != null) {
                        cache.add(new BotTaskLinkParametersRenameProvider(taskState.getBotTaskLink()));
                    } else {
                        String botName = taskState.getSwimlaneBotName();
                        if (botName != null) {
                            // if bot task exists with same as task name
                            BotTask botTask = BotCache.getBotTask(botName, taskState.getName());
                            if (botTask != null && botTask.getType() == BotTaskType.SIMPLE) {
                                cache.add(new BotTaskConfigRenameProvider(botTask));
                            }
                        }
                    }
                }
                List<Action> actions = definition.getChildrenRecursive(Action.class);
                for (Action action : actions) {
                    cache.add(new DelegablePresentation(action, action.getLabel()));
                }
                List<Decision> decisions = definition.getChildren(Decision.class);
                for (Decision decision : decisions) {
                    cache.add(new DelegablePresentation(decision, decision.getName()));
                }
                List<Subprocess> subprocesses = definition.getChildren(Subprocess.class);
                for (Subprocess subprocess : subprocesses) {
                    cache.add(new SubprocessPresentation(subprocess));
                }
                List<Swimlane> swimlaneNodes = definition.getChildren(Swimlane.class);
                for (Swimlane swimlaneNode : swimlaneNodes) {
                    cache.add(new SwimlanePresentation(swimlaneNode));
                }
                List<NodeTypeDefinition> typesWithProvider = NodeRegistry.getTypesWithVariableRenameProvider();
                for (NodeTypeDefinition elementTypeDefinition : typesWithProvider) {
                    List<? extends GraphElement> list = definition.getChildren(elementTypeDefinition.getModelClass());
                    for (GraphElement graphElement : list) {
                        VariableRenameProvider provider = elementTypeDefinition.createVariableRenameProvider();
                        provider.setElement(graphElement);
                        cache.add(provider);
                    }
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog(e.getMessage(), e);
            result.addFatalError(Localization.getString("UnhandledException"));
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
                    List<Change> changes = classPresentation.getChanges(oldVariable, newVariable);
                    cashedChange.addAll(changes.toArray(new Change[changes.size()]));
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog(e.getMessage(), e);
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
