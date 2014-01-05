package ru.runa.gpd.editor;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import ru.runa.gpd.editor.gef.command.ProcessDefinitionRemoveSwimlaneCommand;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ltk.PortabilityRefactoring;
import ru.runa.gpd.ltk.RenameRefactoringWizard;
import ru.runa.gpd.search.VariableSearchQuery;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.dialog.SwimlaneConfigDialog;
import ru.runa.gpd.ui.dialog.UpdateSwimlaneNameDialog;

public class SwimlaneEditorPage extends EditorPartBase {
    private TableViewer tableViewer;
    private Button searchButton;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button renameButton;
    private Button changeButton;
    private Button deleteButton;
    private Button copyButton;
    private Button pasteButton;

    public SwimlaneEditorPage(ProcessEditorBase editor) {
        super(editor);
    }

    @Override
    public void setFocus() {
        super.setFocus();
        updateButtons();
    }

    @Override
    public void createPartControl(Composite parent) {
        SashForm sashForm = createSashForm(parent, SWT.VERTICAL, "DesignerSwimlaneEditorPage.label.swimlanes");
        Composite allSwimlanesComposite = createSection(sashForm, "DesignerSwimlaneEditorPage.label.all_swimlanes");
        tableViewer = new TableViewer(allSwimlanesComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        getToolkit().adapt(tableViewer.getControl(), false, false);
        tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        tableViewer.setLabelProvider(new TableViewerLabelProvider());
        tableViewer.setContentProvider(new ArrayContentProvider());
        createContextMenu(tableViewer.getControl());
        getSite().setSelectionProvider(tableViewer);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("property.name"), Localization.getString("swimlane.initializer") };
        int[] columnWidths = new int[] { 200, 400 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }
        Composite buttonsBar = getToolkit().createComposite(allSwimlanesComposite);
        buttonsBar.setLayout(new GridLayout(1, false));
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        buttonsBar.setLayoutData(gridData);
        addButton(buttonsBar, "button.create", new CreateSwimlaneSelectionListener(), false);
        renameButton = addButton(buttonsBar, "button.rename", new RenameSwimlaneSelectionListener(), true);
        changeButton = addButton(buttonsBar, "button.change", new ChangeSwimlaneSelectionListener(), true);
        copyButton = addButton(buttonsBar, "button.copy", new CopySwimlaneSelectionListener(), true);
        pasteButton = addButton(buttonsBar, "button.paste", new PasteSwimlaneSelectionListener(), true);
        searchButton = addButton(buttonsBar, "button.search", new SearchSwimlaneUsageSelectionListener(), true);
        moveUpButton = addButton(buttonsBar, "button.up", new MoveSwimlaneSelectionListener(true), true);
        moveDownButton = addButton(buttonsBar, "button.down", new MoveSwimlaneSelectionListener(false), true);
        deleteButton = addButton(buttonsBar, "button.delete", new RemoveSwimlaneSelectionListener(), true);
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtons();
            }
        });
        fillViewer();
        updateButtons();
    }

    private void updateButtons() {
        List<?> swimlanes = (List<?>) tableViewer.getInput();
        List<?> selected = ((IStructuredSelection) tableViewer.getSelection()).toList();
        enableAction(searchButton, selected.size() == 1);
        enableAction(changeButton, selected.size() == 1);
        enableAction(moveUpButton, selected.size() == 1 && swimlanes.indexOf(selected.get(0)) > 0);
        enableAction(moveDownButton, selected.size() == 1 && swimlanes.indexOf(selected.get(0)) < swimlanes.size() - 1);
        enableAction(deleteButton, selected.size() > 0);
        enableAction(renameButton, selected.size() == 1);
        enableAction(copyButton, selected.size() > 0);
        boolean pasteEnabled = false;
        if (Clipboard.getDefault().getContents() instanceof List) {
            List<?> list = (List<?>) Clipboard.getDefault().getContents();
            if (list.size() > 0 && list.get(0) instanceof Swimlane) {
                pasteEnabled = true;
            }
        }
        enableAction(pasteButton, pasteEnabled);
    }

    public void select(Swimlane variable) {
        tableViewer.setSelection(new StructuredSelection(variable));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String type = event.getPropertyName();
        if (PropertyNames.PROPERTY_CHILDS_CHANGED.equals(type)) {
            fillViewer();
        } else if (event.getSource() instanceof Swimlane) {
            if (PropertyNames.PROPERTY_NAME.equals(type) || PropertyNames.PROPERTY_CONFIGURATION.equals(type)) {
                tableViewer.refresh(event.getSource());
            }
        }
    }

    private void fillViewer() {
        List<Swimlane> swimlanes = getDefinition().getSwimlanes();
        tableViewer.setInput(swimlanes);
        for (Swimlane swimlane : swimlanes) {
            swimlane.addPropertyChangeListener(this);
        }
        updateButtons();
    }

    @Override
    public void dispose() {
        for (Swimlane swimlane : getDefinition().getSwimlanes()) {
            swimlane.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    private class MoveSwimlaneSelectionListener extends SelectionAdapter {
        private final boolean up;

        public MoveSwimlaneSelectionListener(boolean up) {
            this.up = up;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();
            List<Swimlane> children = swimlane.getParent().getChildren(Swimlane.class);
            int index = children.indexOf(swimlane);
            swimlane.getParent().swapChilds(swimlane, up ? children.get(index - 1) : children.get(index + 1));
            tableViewer.setSelection(selection);
        }
    }

    private void delete(Swimlane swimlane) {
        boolean confirmationRequired = false;
        StringBuffer message = new StringBuffer(Localization.getString("Swimlane.UsedInStates")).append("\n");
        StringBuffer stateNames = new StringBuffer();
        for (SwimlanedNode node : getDefinition().getChildren(SwimlanedNode.class)) {
            if (node.getSwimlaneName() != null && swimlane.getName().equals(node.getSwimlaneName())) {
                stateNames.append(" - ").append(node.getName()).append("\n");
            }
        }
        if (stateNames.length() > 0) {
            confirmationRequired = true;
            message.append(stateNames);
        } else {
            message.append(Localization.getString("Swimlane.NotUsed"));
        }
        message.append("\n");
        List<FormNode> nodesWithVar = ParContentProvider.getFormsWhereVariableUsed(editor.getDefinitionFile(), getDefinition(), swimlane);
        message.append(Localization.getString("Variable.ExistInForms")).append("\n");
        if (nodesWithVar.size() > 0) {
            confirmationRequired = true;
            for (FormNode node : nodesWithVar) {
                message.append(" - ").append(node.getName()).append("\n");
            }
            message.append(Localization.getString("Variable.WillBeRemovedFromFormAuto"));
        } else {
            message.append(Localization.getString("Variable.NoFormsUsed"));
        }
        if (!confirmationRequired || Dialogs.confirm(Localization.getString("confirm.delete"), message.toString())) {
            // remove variable from form validations
            ParContentProvider.rewriteFormValidationsRemoveVariable(editor.getDefinitionFile(), nodesWithVar, swimlane);
            ProcessDefinitionRemoveSwimlaneCommand command = new ProcessDefinitionRemoveSwimlaneCommand();
            command.setProcessDefinition(getDefinition());
            command.setSwimlane(swimlane);
            // TODO Ctrl+Z support (form validation) editor.getCommandStack().execute(command);
            command.execute();
            getDefinition().getSwimlaneGUIConfiguration().removeSwimlanePath(swimlane.getName());
        }
    }

    private class SearchSwimlaneUsageSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();
            VariableSearchQuery query = new VariableSearchQuery(editor.getDefinitionFile(), getDefinition(), swimlane);
            NewSearchUI.runQueryInBackground(query);
        }
    }

    private class RenameSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();
            UpdateSwimlaneNameDialog renameDialog = new UpdateSwimlaneNameDialog(swimlane.getProcessDefinition(), swimlane);
            int result = renameDialog.open();
            String newName = renameDialog.getName();
            boolean useLtk = renameDialog.isProceedRefactoring();
            if (result != IDialogConstants.OK_ID) {
                return;
            }
            Variable oldVariable = new Variable(swimlane);
            swimlane.setName(newName);
            swimlane.setScriptingName(renameDialog.getScriptingName());
            IResource projectRoot = editor.getDefinitionFile().getParent();
            if (useLtk) {
                PortabilityRefactoring ref = new PortabilityRefactoring(editor.getDefinitionFile(), editor.getDefinition(), oldVariable, swimlane);
                useLtk &= ref.isUserInteractionNeeded();
                if (useLtk) {
                    RenameRefactoringWizard wizard = new RenameRefactoringWizard(ref);
                    wizard.setDefaultPageTitle(Localization.getString("Refactoring.variable.name"));
                    RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
                    result = op.run(Display.getCurrent().getActiveShell(), "");
                    if (result != IDialogConstants.OK_ID) {
                        // revert changes
                        swimlane.setName(oldVariable.getName());
                        swimlane.setScriptingName(oldVariable.getScriptingName());
                        return;
                    }
                }
            }
            if (useLtk) {
                IDE.saveAllEditors(new IResource[] { projectRoot }, false);
            }
        }
    }

    private class RemoveSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            List<Swimlane> swimlanes = selection.toList();
            for (Swimlane swimlane : swimlanes) {
                delete(swimlane);
            }
        }
    }

    private class CreateSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            UpdateSwimlaneNameDialog dialog = new UpdateSwimlaneNameDialog(getDefinition(), null);
            if (dialog.open() == IDialogConstants.OK_ID) {
                Swimlane newSwimlane = NodeRegistry.getNodeTypeDefinition(Swimlane.class).createElement(getDefinition(), false);
                newSwimlane.setName(dialog.getName());
                newSwimlane.setScriptingName(dialog.getScriptingName());
                getDefinition().addChild(newSwimlane);
                tableViewer.setSelection(new StructuredSelection(newSwimlane));
            }
        }
    }

    private class ChangeSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();
            String path = getDefinition().getSwimlaneGUIConfiguration().getEditorPath(swimlane.getName());
            SwimlaneConfigDialog dialog = new SwimlaneConfigDialog(getDefinition(), swimlane, path);
            if (dialog.open() == IDialogConstants.OK_ID) {
                swimlane.setDelegationConfiguration(dialog.getConfiguration());
                swimlane.setPublicVisibility(dialog.isPublicVisibility());
                getDefinition().getSwimlaneGUIConfiguration().putSwimlanePath(swimlane.getName(), dialog.getPath());
                tableViewer.setSelection(selection);
            }
        }
    }

    private class CopySwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Clipboard.getDefault().setContents(selection.toList());
        }
    }

    private class PasteSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            List<Swimlane> newSwimlanes = (List<Swimlane>) Clipboard.getDefault().getContents();
            for (Swimlane swimlane : newSwimlanes) {
                boolean add = false;
                Swimlane newSwimlane = getDefinition().getSwimlaneByName(swimlane.getName());
                if (newSwimlane == null) {
                    newSwimlane = NodeRegistry.getNodeTypeDefinition(Swimlane.class).createElement(getDefinition(), false);
                    newSwimlane.setName(swimlane.getName());
                    add = true;
                }
                newSwimlane.setDelegationClassName(swimlane.getDelegationClassName());
                newSwimlane.setDelegationConfiguration(swimlane.getDelegationConfiguration());
                if (add) {
                    getDefinition().addChild(newSwimlane);
                }
            }
        }
    }

    private static class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            Swimlane swimlane = (Swimlane) element;
            switch (index) {
            case 0:
                return swimlane.getName();
            case 1:
                return swimlane.getDelegationConfiguration();
            default:
                return "unknown " + index;
            }
        }

        @Override
        public String getText(Object element) {
            Swimlane swimlane = (Swimlane) element;
            return swimlane.getName();
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
}
