package ru.runa.gpd.editor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.CopyBuffer.ExtraCopyAction;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.dialog.MultipleSelectionDialog;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CopyGraphCommand extends Command {
    private final ProcessDefinition targetDefinition;
    private final IFolder targetFolder;
    private final CopyBuffer copyBuffer;
    private final Map<String, Node> targetNodeMap = Maps.newHashMap();
    private final List<ExtraCopyAction> executedCopyActions = Lists.newArrayList();

    public CopyGraphCommand(ProcessDefinition targetDefinition, IFolder targetFolder) {
        this.targetDefinition = targetDefinition;
        this.targetFolder = targetFolder;
        this.copyBuffer = new CopyBuffer();
    }

    @Override
    public boolean canExecute() {
        return copyBuffer.isValid() && !copyBuffer.getSourceDefinition().equals(targetDefinition);
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
            Set<ExtraCopyAction> copyActions = new HashSet<ExtraCopyAction>();
            List<Node> sourceNodeList = copyBuffer.getSourceNodes();
            // add nodes
            for (Node node : sourceNodeList) {
                if (node instanceof StartState && targetDefinition.getChildren(StartState.class).size() != 0) {
                    continue;
                } else if (node instanceof EndState && targetDefinition.getChildren(EndState.class).size() != 0) {
                    continue;
                }
                Node copy = node.getCopy(targetDefinition);
                if (node instanceof ITimed) {
                    Timer timer = ((ITimed) node).getTimer();
                    if (timer != null) {
                        String variableName = timer.getDelay().getVariableName();
                        if (variableName != null) {
                            Variable variable = VariableUtils.getVariableByName(copyBuffer.getSourceDefinition(), variableName);
                            CopyVariableAction copyAction = new CopyVariableAction(variable);
                            copyActions.add(copyAction);
                        }
                    }
                }
                if (node instanceof Subprocess) {
                    for (VariableMapping mapping : ((Subprocess) node).getVariableMappings()) {
                        Variable variable = VariableUtils.getVariableByName(copyBuffer.getSourceDefinition(), mapping.getProcessVariableName());
                        if (variable != null) {
                            CopyVariableAction copyAction = new CopyVariableAction(variable);
                            copyActions.add(copyAction);
                        }
                        if (VariableMapping.MULTISUBPROCESS_VARIABLE_PLACEHOLDER.equals(mapping.getProcessVariableName())) {
                            variable = VariableUtils.getVariableByName(copyBuffer.getSourceDefinition(), mapping.getSubprocessVariableName());
                            if (variable != null) {
                                CopyVariableAction copyAction = new CopyVariableAction(variable);
                                copyActions.add(copyAction);
                            }
                        }
                    }
                }
                targetNodeMap.put(node.getId(), copy);
                if (node instanceof FormNode) {
                    FormNode formNode = (FormNode) node;
                    if (formNode.hasForm() || formNode.hasFormValidation() || formNode.hasFormScript() || formNode.hasFormTemplate()) {
                        CopyFormFilesAction copyAction = new CopyFormFilesAction(formNode, (FormNode) copy);
                        copyAction.setSourceFolder(copyBuffer.getSourceFolder());
                        copyAction.setTargetFolder(targetFolder);
                        copyActions.add(copyAction);
                    }
                    Map<String, FormVariableAccess> variables = formNode.getFormVariables(copyBuffer.getSourceFolder());
                    for (String varName : variables.keySet()) {
                        Variable variable = VariableUtils.getVariableByName(copyBuffer.getSourceDefinition(), varName);
                        if (variable != null) {
                            CopyVariableAction copyAction = new CopyVariableAction(variable);
                            copyActions.add(copyAction);
                        }
                    }
                }
                if (node instanceof SwimlanedNode) {
                    Swimlane swimlane = ((SwimlanedNode) node).getSwimlane();
                    boolean ignoreSwimlane = targetDefinition instanceof SubprocessDefinition && node instanceof StartState;
                    if (swimlane != null && !ignoreSwimlane) {
                        CopySwimlaneAction copyAction = new CopySwimlaneAction(swimlane);
                        copyActions.add(copyAction);
                    }
                }
            }
            // add transitions
            for (Node node : sourceNodeList) {
                List<Transition> transitions = node.getChildren(Transition.class);
                for (Transition transition : transitions) {
                    Node source = targetNodeMap.get(transition.getSource().getId());
                    Node target = targetNodeMap.get(transition.getTarget().getId());
                    if (source != null && target != null) {
                        Transition copy = transition.getCopy(source);
                        copy.setTarget(target);
                    }
                }
            }
            List<ExtraCopyAction> sortedCopyActions = Lists.newArrayList(copyActions);
            Collections.sort(sortedCopyActions);
            List<ExtraCopyAction> userConfirmedActions = new ArrayList<ExtraCopyAction>();
            for (ExtraCopyAction copyAction : sortedCopyActions) {
                if (copyAction.isUserConfirmationRequired()) {
                    copyAction.setEnabled(false);
                    userConfirmedActions.add(copyAction);
                }
            }
            if (userConfirmedActions.size() > 0) {
                // display dialog with collisions
                MultipleSelectionDialog dialog = new MultipleSelectionDialog(
                        Localization.getString("CopyGraphRewriteDialog.title"), userConfirmedActions);
                if (dialog.open() != IDialogConstants.OK_ID) {
                    for (ExtraCopyAction copyAction : userConfirmedActions) {
                        copyAction.setEnabled(false);
                    }                    
                }
            }
            // run copy actions
            for (ExtraCopyAction copyAction : sortedCopyActions) {
                if (copyAction.isEnabled()) {
                    copyAction.execute();
                    executedCopyActions.add(copyAction);
                }
            }
            // set swimlanes
            for (Map.Entry<String, Node> entry : targetNodeMap.entrySet()) {
                if (entry.getValue() instanceof SwimlanedNode) {
                    boolean ignoreSwimlane = targetDefinition instanceof SubprocessDefinition && entry.getValue() instanceof StartState;
                    if (!ignoreSwimlane) {
                        SwimlanedNode sourceNode = copyBuffer.getSourceDefinition().getGraphElementByIdNotNull(entry.getKey());
                        Swimlane swimlane = targetDefinition.getSwimlaneByName(sourceNode.getSwimlaneName());
                        ((SwimlanedNode) entry.getValue()).setSwimlane(swimlane);
                    }
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
        for (ExtraCopyAction extraCopyAction : executedCopyActions) {
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
        private IFile templateFile;

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
        protected String getChanges() {
            List<String> fileNames = Lists.newArrayList();
            if (targetFormNode.hasForm() && targetFolder.getFile(targetFormNode.getFormFileName()).exists()) {
                fileNames.add(targetFormNode.getFormFileName());
            }
            if (targetFormNode.hasFormValidation() && targetFolder.getFile(targetFormNode.getValidationFileName()).exists()) {
                fileNames.add(targetFormNode.getValidationFileName());
            }
            if (targetFormNode.hasFormScript() && targetFolder.getFile(targetFormNode.getScriptFileName()).exists()) {
                fileNames.add(targetFormNode.getScriptFileName());
            }
            if (fileNames.isEmpty()) {
                return super.getChanges();
            }
            return fileNames.toString();
        }

        @Override
        public void execute() throws CoreException {
            if (sourceFormNode.hasForm()) {
                formFile = copyFile(sourceFormNode.getFormFileName(), targetFormNode.getFormFileName());
            }
            if (sourceFormNode.hasFormValidation()) {
                validationFile = copyFile(sourceFormNode.getValidationFileName(), targetFormNode.getValidationFileName());
            }
            if (sourceFormNode.hasFormScript()) {
                scriptFile = copyFile(sourceFormNode.getScriptFileName(), targetFormNode.getScriptFileName());
            }
            if (targetFormNode.hasFormTemplate() && !targetFolder.getFile(targetFormNode.getTemplateFileName()).exists()) {
                templateFile = copyFile(sourceFormNode.getTemplateFileName(), targetFormNode.getTemplateFileName());
            }
        }

        private IFile copyFile(String sourceFileName, String targetFileName) throws CoreException {
            InputStream is = sourceFolder.getFile(sourceFileName).getContents();
            IFile file = targetFolder.getFile(targetFileName);
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
            if (templateFile != null) {
                templateFile.delete(true, null);
            }
        }
    }

    private class CopySwimlaneAction extends ExtraCopyAction {
        private final Swimlane sourceSwimlane;
        private Swimlane oldSwimlane;
        private Swimlane addedSwimlane;

        public CopySwimlaneAction(Swimlane sourceSwimlane) {
            super(CopyBuffer.GROUP_SWIMLANES, sourceSwimlane.getName());
            this.sourceSwimlane = sourceSwimlane;
        }

        @Override
        protected String getChanges() {
            oldSwimlane = targetDefinition.getSwimlaneByName(sourceSwimlane.getName());
            if (oldSwimlane == null) {
                return null;
            }
            if (!Objects.equal(oldSwimlane.getDelegationClassName(), sourceSwimlane.getDelegationClassName())) {
                return oldSwimlane.getDelegationClassName() + "/" + sourceSwimlane.getDelegationClassName();
            }
            if (!Objects.equal(oldSwimlane.getDelegationConfiguration(), sourceSwimlane.getDelegationConfiguration())) {
                return oldSwimlane.getDelegationConfiguration() + "/" + sourceSwimlane.getDelegationConfiguration();
            }
            return super.getChanges();
        }

        @Override
        public void execute() {
            if (oldSwimlane != null) {
                targetDefinition.removeChild(oldSwimlane);
            }
            addedSwimlane = (Swimlane) sourceSwimlane.getCopy(targetDefinition);
        }

        @Override
        public void undo() {
            targetDefinition.removeChild(addedSwimlane);
            if (oldSwimlane != null) {
                targetDefinition.addChild(oldSwimlane);
            }
        }
    }

    private class CopyVariableAction extends ExtraCopyAction {
        private final Variable sourceVariable;
        private Variable oldVariable;
        private Variable addedVariable;
        private VariableUserType addedUserType;

        public CopyVariableAction(Variable sourceVariable) {
            super(CopyBuffer.GROUP_VARIABLES, sourceVariable.getName());
            this.sourceVariable = sourceVariable;
        }

        @Override
        protected String getChanges() {
            oldVariable = VariableUtils.getVariableByName(targetDefinition, sourceVariable.getName());
            if (oldVariable == null) {
                return null;
            }
            if (!Objects.equal(oldVariable.getFormat(), sourceVariable.getFormat())) {
                return oldVariable.getFormat() + "/" + sourceVariable.getFormat();
            }
            return super.getChanges();
        }

        @Override
        public void execute() {
            if (oldVariable != null) {
                targetDefinition.removeChild(oldVariable);
            }
            addedVariable = (Variable) sourceVariable.getCopy(targetDefinition);
            if (addedVariable.isComplex() && targetDefinition.getVariableUserType(addedVariable.getUserType().getName()) != null) {
                addedUserType = addedVariable.getUserType();
                targetDefinition.addVariableUserType(addedUserType);
            }
        }

        @Override
        public void undo() {
            targetDefinition.removeChild(addedVariable);
            if (addedUserType != null) {
                targetDefinition.removeVariableUserType(addedUserType);
            }
            if (oldVariable != null) {
                targetDefinition.addChild(oldVariable);
            }
        }
    }

}
