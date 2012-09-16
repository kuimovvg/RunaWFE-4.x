package ru.runa.bpm.ui.common.command;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.PluginConstants;
import ru.runa.bpm.ui.common.model.Action;
import ru.runa.bpm.ui.common.model.Active;
import ru.runa.bpm.ui.common.model.Bendpoint;
import ru.runa.bpm.ui.common.model.Decision;
import ru.runa.bpm.ui.common.model.EndState;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ITimed;
import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.ProcessDefinition;
import ru.runa.bpm.ui.common.model.StartState;
import ru.runa.bpm.ui.common.model.State;
import ru.runa.bpm.ui.common.model.Subprocess;
import ru.runa.bpm.ui.common.model.Swimlane;
import ru.runa.bpm.ui.common.model.SwimlanedNode;
import ru.runa.bpm.ui.common.model.Transition;
import ru.runa.bpm.ui.common.model.Variable;
import ru.runa.bpm.ui.dialog.CopyGraphRewriteDialog;
import ru.runa.bpm.ui.editor.CopyBuffer;
import ru.runa.bpm.ui.editor.GEFElementCreationFactory;
import ru.runa.bpm.ui.editor.CopyBuffer.ExtraCopyAction;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.VariableMapping;

public class CopyGraphCommand extends Command {

    private final ProcessDefinition targetDefinition;
    private final IFolder targetFolder;
    private final CopyBuffer copyBuffer;
    private final Map<String, Node> targetNodeList = new HashMap<String, Node>();
    private final List<ExtraCopyAction> executedActionsList = new ArrayList<ExtraCopyAction>();

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
        return Messages.getString("Action.paste");
    }

    @Override
    public void execute() {
        try {
            if (!copyBuffer.getSourceDefinition().getJpdlVersion().equals(targetDefinition.getJpdlVersion())) {
                MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.getString("message.warning"), Messages
                        .getString("CopyBuffer.DifferentVersion.warning"));
                return;
            }
            Set<ExtraCopyAction> elements = new HashSet<ExtraCopyAction>();
            List<Node> sourceNodeList = copyBuffer.extractSourceNodes();
            // add nodes
            for (Node node : sourceNodeList) {
                GEFElementCreationFactory factory = null;
                if (node instanceof StartState && targetDefinition.getChildren(StartState.class).size() == 0) {
                    factory = new GEFElementCreationFactory("start-state", targetDefinition);
                } else if (node instanceof EndState && targetDefinition.getChildren(EndState.class).size() == 0) {
                    factory = new GEFElementCreationFactory("end-state", targetDefinition);
                } else if (targetDefinition.getNodeByName(node.getName()) == null) {
                    factory = new GEFElementCreationFactory(node.getTypeName(), targetDefinition);
                }
                if (factory != null) {
                    Node copy = (Node) factory.getNewObject();
                    copy.setName(node.getName());
                    copy.setDescription(node.getDescription());
                    copy.setConstraint(node.getConstraint());
                    if (node instanceof ITimed && ((ITimed) node).timerExist()) {
                        ((ITimed) copy).setDueDate(((ITimed) node).getDuration().getDuration());
                        String variableName = ((ITimed) node).getDuration().getVariableName();
                        if (variableName != null) {
                            Variable variable = copyBuffer.getSourceDefinition().getVariablesMap().get(variableName);
                            CopyVariableAction copyAction = new CopyVariableAction(variable);
                            elements.add(copyAction);
                        }
                    }
                    if (node instanceof State) {
                        ((State) copy).setMinimizedView(((State) node).isMinimizedView());
                    }
                    if (node instanceof Decision) {
                        copy.setDelegationConfiguration(node.getDelegationConfiguration());
                        copy.setDelegationClassName(node.getDelegationClassName());
                    }
                    if (node instanceof Subprocess) {
                        ((Subprocess) copy).setSubProcessName(((Subprocess) node).getSubProcessName());
                        List<VariableMapping> variables = ((Subprocess) node).getVariablesList();
                        ((Subprocess) copy).setVariablesList(variables);
                        for (VariableMapping varMapping : variables) {
                            Variable variable = copyBuffer.getSourceDefinition().getVariablesMap().get(varMapping.getProcessVariable());
                            if (variable != null) {
                                CopyVariableAction copyAction = new CopyVariableAction(variable);
                                elements.add(copyAction);
                            }
                        }
                        copy.setDelegationClassName(node.getDelegationClassName());
                    }
                    targetDefinition.addChild(copy);

                    targetNodeList.put(copy.getName(), copy);

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
                            Variable variable = copyBuffer.getSourceDefinition().getVariablesMap().get(varName);
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
                        List<? extends ru.runa.bpm.ui.common.model.Action> actions = ((Active) node).getActions();
                        for (ru.runa.bpm.ui.common.model.Action action : actions) {
                            AddActionHandlerAction copyAction = new AddActionHandlerAction((Active) copy, action);
                            elements.add(copyAction);
                        }
                    }
                }
            }

            // add transitions
            GEFElementCreationFactory trFactory = new GEFElementCreationFactory("transition", targetDefinition);
            for (Node node : sourceNodeList) {
                List<Transition> transitions = node.getChildren(Transition.class);
                for (Transition transition : transitions) {
                    Node source = targetNodeList.get(transition.getSource().getName());
                    Node target = targetNodeList.get(transition.getTarget().getName());
                    if (source != null && target != null) {
                        Transition tr = (Transition) trFactory.getNewObject(source);
                        tr.setName(transition.getName());
                        tr.setTarget(target);
                        for (Bendpoint bp : transition.getBendpoints()) {
                            tr.getBendpoints().add(new Bendpoint(bp.getX(), bp.getY()));
                        }
                        source.addLeavingTransition(tr);

                        for (ru.runa.bpm.ui.common.model.Action action : transition.getActions()) {
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
            for (Node node : targetNodeList.values()) {
                if (node instanceof SwimlanedNode) {
                    SwimlanedNode sourceNode = (SwimlanedNode) copyBuffer.getSourceDefinition().getNodeByNameNotNull(node.getName());
                    Swimlane swimlane = targetDefinition.getSwimlaneByName(sourceNode.getSwimlaneName());
                    ((SwimlanedNode) node).setSwimlane(swimlane);
                }
            }
        } catch (Exception e) {
            DesignerLogger.logError("'Paste' operation failed", e);
        }
    }

    @Override
    public void undo() {
        // remove nodes
        for (Node node : targetNodeList.values()) {
            targetDefinition.removeChild(node);
        }
        // undo actions
        for (ExtraCopyAction extraCopyAction : executedActionsList) {
            try {
                extraCopyAction.undo();
            } catch (Exception e) {
                DesignerLogger.logError("Unable undo operation for action " + extraCopyAction, e);
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
                targetFormNode.setFormFileName(sourceFormNode.getFormFileName());
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
            file.setCharset(PluginConstants.UTF_ENCODING, null);
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
            GEFElementCreationFactory swimlaneFactory = new GEFElementCreationFactory("swimlane", targetDefinition);
            this.swimlane = (Swimlane) swimlaneFactory.getNewObject();
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
            this.oldVariable = targetDefinition.getVariablesMap().get(variable.getName());
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
            GEFElementCreationFactory actionFactory = new GEFElementCreationFactory("action", targetDefinition);
            this.action = (Action) actionFactory.getNewObject((GraphElement) active);
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
