package ru.runa.gpd.editor;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.command.ProcessDefinitionRemoveVariablesCommand;
import ru.runa.gpd.handler.LocalizationRegistry;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.NotificationMessages;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ltk.PortabilityRefactoring;
import ru.runa.gpd.ltk.RenameRefactoringWizard;
import ru.runa.gpd.search.GPDSearchQuery;
import ru.runa.gpd.ui.dialog.CreateVariableDialog;
import ru.runa.gpd.ui.dialog.UpdateVariableNameDialog;

public class VariableEditorPage extends EditorPartBase {
    private TableViewer tableViewer;
    private Button searchButton;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button renameButton;
    private Button changeButton;
    private Button deleteButton;
    private Button copyButton;
    private Button pasteButton;

    public VariableEditorPage(ProcessEditorBase editor) {
        super(editor);
    }

    @Override
    public void setFocus() {
        super.setFocus();
        updateButtons();
    }

    @Override
    public void createPartControl(Composite parent) {
        SashForm sashForm = createToolkit(parent, "DesignerVariableEditorPage.label.variables");
        Composite allVariablesComposite = createSection(sashForm, "DesignerVariableEditorPage.label.all_variables");
        tableViewer = new TableViewer(allVariablesComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        toolkit.adapt(tableViewer.getControl(), false, false);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = 100;
        tableViewer.getControl().setLayoutData(gridData);
        tableViewer.setLabelProvider(new TableViewerLabelProvider());
        tableViewer.setContentProvider(new ArrayContentProvider());
        createContextMenu(tableViewer.getControl());
        getSite().setSelectionProvider(tableViewer);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("property.name"), Localization.getString("Variable.property.format"),
                Localization.getString("Variable.property.defaultValue") };
        int[] columnWidths = new int[] { 200, 200, 200 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }
        Composite buttonsBar = toolkit.createComposite(allVariablesComposite);
        buttonsBar.setLayout(new GridLayout(1, false));
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        buttonsBar.setLayoutData(gridData);
        addButton(buttonsBar, "button.create", new CreateVariableSelectionListener(), false);
        renameButton = addButton(buttonsBar, "button.rename", new RenameVariableSelectionListener(), true);
        changeButton = addButton(buttonsBar, "button.change", new ChangeVariableSelectionListener(), true);
        copyButton = addButton(buttonsBar, "button.copy", new CopyVariableSelectionListener(), true);
        pasteButton = addButton(buttonsBar, "button.paste", new PasteVariableSelectionListener(), true);
        searchButton = addButton(buttonsBar, "button.search", new SearchVariableUsageSelectionListener(), true);
        moveUpButton = addButton(buttonsBar, "button.up", new MoveVariableSelectionListener(true), true);
        moveDownButton = addButton(buttonsBar, "button.down", new MoveVariableSelectionListener(false), true);
        deleteButton = addButton(buttonsBar, "button.delete", new RemoveVariableSelectionListener(), true);
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtons();
            }
        });
        fillViewer();
        updateButtons();
        int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
        Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
        tableViewer.addDragSupport(dndOperations, transfers, new MoveVariableDragListener(tableViewer));
        tableViewer.addDropSupport(dndOperations, transfers, new MoveVariableDropListener(tableViewer));
    }

    private void updateButtons() {
        List<?> variables = (List<?>) tableViewer.getInput();
        List<?> selected = ((IStructuredSelection) tableViewer.getSelection()).toList();
        enableAction(searchButton, selected.size() == 1);
        enableAction(changeButton, selected.size() == 1);
        enableAction(moveUpButton, selected.size() == 1 && variables.indexOf(selected.get(0)) > 0);
        enableAction(moveDownButton, selected.size() == 1 && variables.indexOf(selected.get(0)) < variables.size() - 1);
        enableAction(deleteButton, selected.size() > 0);
        enableAction(renameButton, selected.size() == 1);
        enableAction(copyButton, selected.size() > 0);
        boolean pasteEnabled = false;
        if (Clipboard.getDefault().getContents() instanceof List) {
            List<?> list = (List<?>) Clipboard.getDefault().getContents();
            if (list.size() > 0 && list.get(0) instanceof Variable) {
                pasteEnabled = true;
            }
        }
        enableAction(pasteButton, pasteEnabled);
    }

    public void select(Variable variable) {
        tableViewer.setSelection(new StructuredSelection(variable));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String type = evt.getPropertyName();
        if (NotificationMessages.NODE_CHILDS_CHANGED.equals(type)) {
            fillViewer();
        } else if (evt.getSource() instanceof Variable) {
            if (NotificationMessages.PROPERTY_NAME.equals(type) || NotificationMessages.PROPERTY_FORMAT.equals(type) || NotificationMessages.PROPERTY_DEFAULT_VALUE.equals(type)) {
                tableViewer.refresh(evt.getSource());
            }
        }
    }

    private void fillViewer() {
        List<Variable> variables = getDefinition().getVariablesList();
        tableViewer.setInput(variables);
        for (Variable var : variables) {
            var.addPropertyChangeListener(this);
        }
        updateButtons();
    }

    @Override
    public void dispose() {
        for (Variable var : getDefinition().getVariablesList()) {
            var.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    private class MoveVariableSelectionListener extends SelectionAdapter {
        private final boolean up;

        public MoveVariableSelectionListener(boolean up) {
            this.up = up;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Variable variable = (Variable) selection.getFirstElement();
            List<Variable> children = getDefinition().getVariablesList();
            int index = children.indexOf(variable);
            getDefinition().swapChilds(variable, up ? children.get(index - 1) : children.get(index + 1));
            tableViewer.setSelection(selection);
            // updateButtons();
        }
    }

    private void delete(Variable variable) {
        List<FormNode> nodesWithVar = ParContentProvider.getFormsWhereVariableUsed(editor.getDefinitionFile(), getDefinition(), variable);
        StringBuffer formNames = new StringBuffer(Localization.getString("Variable.ExistInForms")).append("\n");
        if (nodesWithVar.size() > 0) {
            for (FormNode node : nodesWithVar) {
                formNames.append(" - ").append(node.getName()).append("\n");
            }
            formNames.append(Localization.getString("Variable.WillBeRemovedFromFormAuto"));
        } else {
            formNames.append(Localization.getString("Variable.NoFormsUsed"));
        }
        if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), Localization.getString("ConfirmDelete"), formNames.toString())) {
            // remove variable from form validations
            ParContentProvider.rewriteFormValidationsRemoveVariable(editor.getDefinitionFile(), nodesWithVar, variable);
            // remove variable from definition
            ProcessDefinitionRemoveVariablesCommand command = new ProcessDefinitionRemoveVariablesCommand();
            command.setProcessDefinition(getDefinition());
            command.setVariable(variable);
            // TODO GEF editor.getCommandStack().execute(command);
            command.execute();
        }
    }

    private class SearchVariableUsageSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                Variable variable = (Variable) selection.getFirstElement();
                GPDSearchQuery query = new GPDSearchQuery(editor.getDefinitionFile(), getDefinition(), variable.getName());
                NewSearchUI.runQueryInBackground(query);
            } catch (Exception ex) {
                PluginLogger.logError(ex);
            }
        }
    }

    private class RenameVariableSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Variable variable = (Variable) selection.getFirstElement();
            UpdateVariableNameDialog dialog = new UpdateVariableNameDialog(editor.getDefinition());
            dialog.setName(variable.getName());
            int result = dialog.open();
            if (result != IDialogConstants.OK_ID) {
                return;
            }
            String replacement = dialog.getName();
            IResource projectRoot = editor.getDefinitionFile().getParent();
            PortabilityRefactoring ref = new PortabilityRefactoring(editor.getDefinitionFile(), editor.getDefinition(), variable.getName(), replacement);
            boolean useLtk = ref.isUserInteractionNeeded();
            if (useLtk) {
                RenameRefactoringWizard wizard = new RenameRefactoringWizard(ref);
                wizard.setDefaultPageTitle(Localization.getString("Refactoring.variable.name"));
                RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
                try {
                    result = op.run(Display.getCurrent().getActiveShell(), "");
                    if (result != IDialogConstants.OK_ID) {
                        return;
                    }
                } catch (InterruptedException ex) {
                    // operation was canceled
                }
            }
            variable.setName(replacement);
            if (useLtk) {
                IDE.saveAllEditors(new IResource[] { projectRoot }, false);
            }
        }
    }

    private class RemoveVariableSelectionListener extends SelectionAdapter {
        @SuppressWarnings("unchecked")
        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            List<Variable> variables = selection.toList();
            for (Variable variable : variables) {
                try {
                    delete(variable);
                } catch (Exception e1) {
                    PluginLogger.logError(e1);
                }
            }
        }
    }

    private class CreateVariableSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            CreateVariableDialog dialog = new CreateVariableDialog(getDefinition(), null);
            if (dialog.open() == IDialogConstants.OK_ID) {
                Variable variable = new Variable(dialog.getName(), dialog.getType(), dialog.isPublicVisibility(), dialog.getDefaultValue());
                getDefinition().addVariable(variable);
                IStructuredSelection selection = new StructuredSelection(variable);
                tableViewer.setSelection(selection);
            }
        }
    }

    private class ChangeVariableSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Variable variable = (Variable) selection.getFirstElement();
            CreateVariableDialog dialog = new CreateVariableDialog(getDefinition(), variable);
            if (dialog.open() == IDialogConstants.OK_ID) {
                variable.setFormat(dialog.getType());
                variable.setPublicVisibility(dialog.isPublicVisibility());
                variable.setDefaultValue(dialog.getDefaultValue());
                tableViewer.setSelection(selection);
            }
        }
    }

    private class CopyVariableSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Clipboard.getDefault().setContents(selection.toList());
        }
    }

    private class PasteVariableSelectionListener extends SelectionAdapter {
        @SuppressWarnings("unchecked")
        @Override
        public void widgetSelected(SelectionEvent e) {
            List<Variable> newVariables = (List<Variable>) Clipboard.getDefault().getContents();
            for (Variable variable : newVariables) {
                Variable newVariable = getDefinition().getVariablesMap().get(variable.getName());
                if (newVariable == null) {
                    newVariable = new Variable(variable);
                    getDefinition().addVariable(newVariable);
                } else {
                    newVariable.setFormat(variable.getFormat());
                }
            }
        }
    }

    private static class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            Variable variable = (Variable) element;
            switch (index) {
            case 0:
                return variable.getName();
            case 1:
                return LocalizationRegistry.getLabel(variable.getFormat());
            case 2:
                if (variable.getDefaultValue() == null) {
                    return "";
                }
                return variable.getDefaultValue();
            default:
                return "unknown " + index;
            }
        }

        @Override
        public String getText(Object element) {
            Variable variable = (Variable) element;
            return variable.getName();
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    private class MoveVariableDragListener extends DragSourceAdapter {
        private final TableViewer viewer;

        public MoveVariableDragListener(TableViewer viewer) {
            this.viewer = viewer;
        }

        @Override
        public void dragSetData(DragSourceEvent event) {
            IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            Variable firstElement = (Variable) selection.getFirstElement();
            if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
                event.data = firstElement.getName();
            }
        }
    }

    private class MoveVariableDropListener extends ViewerDropAdapter {
        public MoveVariableDropListener(Viewer viewer) {
            super(viewer);
        }

        @Override
        public void drop(DropTargetEvent event) {
            Variable variable1 = getDefinition().getVariablesMap().get(event.data);
            Variable beforeVariable = (Variable) determineTarget(event);
            getDefinition().changeChildIndex(variable1, beforeVariable);
            super.drop(event);
        }

        @Override
        public boolean performDrop(Object data) {
            return false;
        }

        @Override
        public boolean validateDrop(Object target, int operation, TransferData transferType) {
            return true;
        }
    }
}
