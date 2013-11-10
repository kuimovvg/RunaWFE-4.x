package ru.runa.gpd.editor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.CopyBuffer.ExtraCopyAction;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.ActionImpl;
import ru.runa.gpd.lang.model.Active;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.dialog.CopyGraphRewriteDialog;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.VariableMapping;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CopyGraphCommand extends Command {
    private final ProcessDefinition targetDefinition;
    private final IFolder targetFolder;
    private final CopyBuffer copyBuffer;
    private final Map<String, Node> targetNodeMap = Maps.newHashMap();
    private final List<ExtraCopyAction> executedActionsList = Lists.newArrayList();

    public CopyGraphCommand(ProcessDefinition targetDefinition, IFolder targetFolder) {
        this.targetDefinition = targetDefinition;
        this.targetFolder = targetFolder;
        this.copyBuffer = new CopyBuffer();
    }

    @Override
    public boolean canExecute() {
        return copyBuffer.isValid();
    }

    @Override
    public String getLabel() {
        return Localization.getString("button.paste");
    }

    @Override
    public void execute() {
        try {
            if (!copyBuffer.getSourceDefinition().getLanguage().equals(targetDefinition.getLanguage())) {
                Dialogs.warning(Localization.getString("CopyBuffer.DifferentVersion.warning"));
                return;
            }
            Set<ExtraCopyAction> elements = new HashSet<ExtraCopyAction>();
            List<Node> sourceNodeList = copyBuffer.getSourceNodes();
            // add nodes
            for (Node node : sourceNodeList) {
                NodeTypeDefinition definition = null;
                if (node instanceof StartState && targetDefinition.getChildren(StartState.class).size() == 0) {
                    definition = NodeRegistry.getNodeTypeDefinition(StartState.class);
                } else if (node instanceof EndState && targetDefinition.getChildren(EndState.class).size() == 0) {
                    definition = NodeRegistry.getNodeTypeDefinition(EndState.class);
                } else if (targetDefinition.getGraphElementById(node.getId()) == null) {
                    definition = NodeRegistry.getNodeTypeDefinition(node.getClass());
                }
                if (definition != null) {
                    Node copy = definition.createElement(targetDefinition);
                    copy.setName(node.getName());
                    copy.setDescription(node.getDescription());
                    copy.setConstraint(node.getConstraint());
                    if (node instanceof ITimed) {
                        Timer timer = ((ITimed) node).getTimer();
                        if (timer != null) {
                            Timer copyTimer = new Timer();
                            copyTimer.setDelay(new Duration(timer.getDelay()));
                            String variableName = timer.getDelay().getVariableName();
                            if (variableName != null) {
                                Variable variable = copyBuffer.getSourceDefinition().getVariable(variableName, false);
                                CopyVariableAction copyAction = new CopyVariableAction(variable);
                                elements.add(copyAction);
                            }
                            copy.addChild(copyTimer);
                        }
                    }
                    node.setMinimizedView(node.isMinimizedView());
                    if (node instanceof Decision) {
                        copy.setDelegationConfiguration(node.getDelegationConfiguration());
                        copy.setDelegationClassName(node.getDelegationClassName());
                    }
                    if (node instanceof Subprocess) {
                        ((Subprocess) copy).setSubProcessName(((Subprocess) node).getSubProcessName());
                        List<VariableMapping> variables = ((Subprocess) node).getVariableMappings();
                        ((Subprocess) copy).setVariableMappings(variables);
                        for (VariableMapping varMapping : variables) {
                            Variable variable = copyBuffer.getSourceDefinition().getVariable(varMapping.getProcessVariableName(), false);
                            if (variable != null) {
                                CopyVariableAction copyAction = new CopyVariableAction(variable);
                                elements.add(copyAction);
                            }
                        }
                        copy.setDelegationClassName(node.getDelegationClassName());
                    }
                    targetDefinition.addChild(copy);
                    targetNodeMap.put(node.getId(), copy);
                    if (node instanceof FormNode) {
                        FormNode formNode = (FormNode) node;
                        if (formNode.hasForm() || formNode.hasFormValidation()) {
                            CopyFormFilesAction copyAction = new CopyFormFilesAction(formNode, (FormNode) copy);
                            copyAction.setSourceFolder(copyBuffer.getSourceFolder());
                            copyAction.setTargetFolder(targetFolder);
                            elements.add(copyAction);
                        }
                        Map<String, Integer> variables = formNode.getFormVariables(copyBuffer.getSourceFolder());
                        for (String varName : variables.keySet()) {
                            Variable variable = copyBuffer.getSourceDefinition().getVariable(varName, false);
                            if (variable != null) {
                                CopyVariableAction copyAction = new CopyVariableAction(variable);
                                elements.add(copyAction);
                            }
                        }
                    }
                    if (node instanceof SwimlanedNode) {
                        Swimlane swimlane = ((SwimlanedNode) node).getSwimlane();
                        if (swimlane != null) {
                            CopySwimlaneAction copyAction = new CopySwimlaneAction(swimlane);
                            elements.add(copyAction);
                        }
                    }
                    if (node instanceof Active) {
                        List<? extends ru.runa.gpd.lang.model.Action> actions = ((Active) node).getActions();
                        for (ru.runa.gpd.lang.model.Action action : actions) {
                            AddActionHandlerAction copyAction = new AddActionHandlerAction((Active) copy, action);
                            elements.add(copyAction);
                        }
                    }
                }
            }
            // add transitions
            NodeTypeDefinition definition = NodeRegistry.getNodeTypeDefinition(Transition.class);
            for (Node node : sourceNodeList) {
                List<Transition> transitions = node.getChildren(Transition.class);
                for (Transition transition : transitions) {
                    Node source = targetNodeMap.get(transition.getSource().getId());
                    Node target = targetNodeMap.get(transition.getTarget().getId());
                    if (source != null && target != null) {
                        Transition tr = definition.createElement(source);
                        tr.setName(transition.getName());
                        tr.setTarget(target);
                        for (Point bp : transition.getBendpoints()) {
                            tr.getBendpoints().add(bp.getCopy());
                        }
                        source.addLeavingTransition(tr);
                        for (ru.runa.gpd.lang.model.Action action : transition.getActions()) {
                            AddActionHandlerAction copyAction = new AddActionHandlerAction(tr, action);
                            elements.add(copyAction);
                        }
                    }
                }
            }
            List<ExtraCopyAction> userConfirmedActions = new ArrayList<ExtraCopyAction>();
            for (ExtraCopyAction copyAction : elements) {
                if (copyAction.isUserConfirmationRequired()) {
                    copyAction.setEnabled(false);
                    userConfirmedActions.add(copyAction);
                }
            }
            if (userConfirmedActions.size() > 0) {
                // display dialog with collisions
                CopyGraphRewriteDialog dialog = new CopyGraphRewriteDialog(userConfirmedActions);
                dialog.open();
            }
            // run copy actions
            for (ExtraCopyAction copyAction : elements) {
                if (copyAction.isEnabled()) {
                    copyAction.execute();
                    executedActionsList.add(copyAction);
                }
            }
            // set swimlanes
            for (Map.Entry<String, Node> entry : targetNodeMap.entrySet()) {
                if (entry.getValue() instanceof SwimlanedNode) {
                    SwimlanedNode sourceNode = copyBuffer.getSourceDefinition().getGraphElementByIdNotNull(entry.getKey());
                    Swimlane swimlane = targetDefinition.getSwimlaneByName(sourceNode.getSwimlaneName());
                    ((SwimlanedNode) entry.getValue()).setSwimlane(swimlane);
                }
            }
        } catch (Exception e) {
            PluginLogger.logError("'Paste' operation failed", e);
        }
    }

    @Override
    public void undo() {
        // remove nodes
        for (Node node : targetNodeMap.values()) {
            targetDefinition.removeChild(node);
        }
        // undo actions
        for (ExtraCopyAction extraCopyAction : executedActionsList) {
            try {
                extraCopyAction.undo();
            } catch (Exception e) {
                PluginLogger.logError("Unable undo operation for action " + extraCopyAction, e);
            }
        }
    }

    private class CopyFormFilesAction extends ExtraCopyAction {
        private final FormNode sourceFormNode;
        private final FormNode targetFormNode;
        private IFolder sourceFolder;
        private IFolder targetFolder;
        private IFile formFile;
        private IFile validationFile;
        private IFile scriptFile;

        public CopyFormFilesAction(FormNode sourceFormNode, FormNode targetFormNode) {
            super(CopyBuffer.GROUP_FORM_FILES, sourceFormNode.getName());
            this.sourceFormNode = sourceFormNode;
            this.targetFormNode = targetFormNode;
        }

        public void setSourceFolder(IFolder sourceFolder) {
            this.sourceFolder = sourceFolder;
        }

        public void setTargetFolder(IFolder targetFolder) {
            this.targetFolder = targetFolder;
        }

        @Override
        public boolean isUserConfirmationRequired() {
            if (sourceFormNode.hasForm() && targetFolder.getFile(sourceFormNode.getFormFileName()).exists()) {
                return true;
            }
            if (sourceFormNode.hasFormValidation() && targetFolder.getFile(sourceFormNode.getValidationFileName()).exists()) {
                return true;
            }
            if (sourceFormNode.hasFormScript() && targetFolder.getFile(sourceFormNode.getScriptFileName()).exists()) {
                return true;
            }
            return false;
        }

        @Override
        public void execute() throws CoreException {
            if (sourceFormNode.hasForm()) {
                formFile = copyFile(sourceFormNode.getFormFileName());
                targetFormNode.setFormFileName(sourceFormNode.getFormFileName()); // TODO
                targetFormNode.setFormType(sourceFormNode.getFormType());
            }
            if (sourceFormNode.hasFormValidation()) {
                validationFile = copyFile(sourceFormNode.getValidationFileName());
                targetFormNode.setValidationFileName(sourceFormNode.getValidationFileName());
            }
            if (sourceFormNode.hasFormScript()) {
                scriptFile = copyFile(sourceFormNode.getScriptFileName());
                targetFormNode.setScriptFileName(sourceFormNode.getScriptFileName());
            }
        }

        private IFile copyFile(String fileName) throws CoreException {
            // TODO copy safely (length of file names)
            InputStream is = sourceFolder.getFile(fileName).getContents();
            IFile file = targetFolder.getFile(fileName);
            if (file.exists()) {
                file.delete(true, null);
            }
            file.create(is, true, null);
            file.setCharset(Charsets.UTF_8.name(), null);
            return file;
        }

        @Override
        public void undo() throws CoreException {
            if (formFile != null) {
                formFile.delete(true, null);
            }
            if (validationFile != null) {
                validationFile.delete(true, null);
            }
            if (scriptFile != null) {
                scriptFile.delete(true, null);
            }
        }
    }

    private class CopySwimlaneAction extends ExtraCopyAction {
        private Swimlane oldSwimlane;
        private final Swimlane swimlane;

        public CopySwimlaneAction(Swimlane sourceSwimlane) {
            super(CopyBuffer.GROUP_SWIMLANES, sourceSwimlane.getName());
            this.swimlane = NodeRegistry.getNodeTypeDefinition(Swimlane.class).createElement(targetDefinition);
            this.swimlane.setName(sourceSwimlane.getName());
            this.swimlane.setDelegationClassName(sourceSwimlane.getDelegationClassName());
            this.swimlane.setDelegationConfiguration(sourceSwimlane.getDelegationConfiguration());
        }

        @Override
        public boolean isUserConfirmationRequired() {
            return swimlane.getName() == null;
        }

        @Override
        public void execute() {
            oldSwimlane = targetDefinition.getSwimlaneByName(getName());
            if (oldSwimlane != null) {
                targetDefinition.removeSwimlane(oldSwimlane);
                swimlane.setName(getName());
            }
            targetDefinition.addSwimlane(swimlane);
        }

        @Override
        public void undo() {
            targetDefinition.removeSwimlane(swimlane);
            if (oldSwimlane != null) {
                targetDefinition.addSwimlane(oldSwimlane);
            }
        }
    }

    private class CopyVariableAction extends ExtraCopyAction {
        private Variable oldVariable;
        private final Variable variable;

        public CopyVariableAction(Variable sourceVariable) {
            super(CopyBuffer.GROUP_VARIABLES, sourceVariable.getName());
            this.variable = new Variable(sourceVariable);
        }

        @Override
        public boolean isUserConfirmationRequired() {
            return targetDefinition.getVariableNames(true).contains(variable.getName());
        }

        @Override
        public void execute() {
            this.oldVariable = targetDefinition.getVariable(variable.getName(), false);
            if (oldVariable != null) {
                targetDefinition.removeVariable(oldVariable);
            }
            targetDefinition.addVariable(variable);
        }

        @Override
        public void undo() {
            targetDefinition.removeVariable(variable);
            if (oldVariable != null) {
                targetDefinition.addVariable(oldVariable);
            }
        }
    }

    private class AddActionHandlerAction extends ExtraCopyAction {
        private final Active active;
        private final Action action;

        public AddActionHandlerAction(Active active, Action action) {
            super(CopyBuffer.GROUP_ACTION_HANDLERS, action.toString());
            this.active = active;
            this.action = NodeRegistry.getNodeTypeDefinition(ActionImpl.class).createElement((GraphElement) active);
            this.action.setDelegationClassName(action.getDelegationClassName());
            this.action.setDelegationConfiguration(action.getDelegationConfiguration());
        }

        @Override
        public void execute() {
            active.addAction(action, -1);
        }

        @Override
        public void undo() {
            active.removeAction(action);
        }
    }
}
