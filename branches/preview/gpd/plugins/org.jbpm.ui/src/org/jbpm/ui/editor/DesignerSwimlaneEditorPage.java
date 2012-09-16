package ru.runa.bpm.ui.editor;

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
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.common.command.ProcessDefinitionRemoveSwimlaneCommand;
import ru.runa.bpm.ui.common.model.NotificationMessages;
import ru.runa.bpm.ui.common.model.Swimlane;
import ru.runa.bpm.ui.common.model.SwimlanedNode;
import ru.runa.bpm.ui.dialog.SwimlaneConfigDialog;
import ru.runa.bpm.ui.dialog.UpdateSwimlaneNameDialog;
import ru.runa.bpm.ui.editor.ltk.PortabilityRefactoring;
import ru.runa.bpm.ui.editor.ltk.RenameRefactoringWizard;
import ru.runa.bpm.ui.editor.search.GPDSearchQuery;
import ru.runa.bpm.ui.resource.Messages;

public class DesignerSwimlaneEditorPage extends EditorPartBase {

    private TableViewer tableViewer;
    private Button searchButton;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button renameButton;
    private Button changeButton;
    private Button deleteButton;
    private Button copyButton;
    private Button pasteButton;

    public DesignerSwimlaneEditorPage(DesignerEditor editor) {
        super(editor);
    }

    @Override
    public void setFocus() {
        super.setFocus();
        updateButtons();
    }

    @Override
    public void createPartControl(Composite parent) {
        SashForm sashForm = createToolkit(parent, "DesignerSwimlaneEditorPage.label.swimlanes");

        Composite allSwimlanesComposite = createSection(sashForm, "DesignerSwimlaneEditorPage.label.all_swimlanes");

        tableViewer = new TableViewer(allSwimlanesComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        toolkit.adapt(tableViewer.getControl(), false, false);
        tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        tableViewer.setLabelProvider(new TableViewerLabelProvider());
        tableViewer.setContentProvider(new ArrayContentProvider());
        createContextMenu(tableViewer.getControl());
        getSite().setSelectionProvider(tableViewer);

        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Messages.getString("property.name"), Messages.getString("swimlane.initializer") };
        int[] columnWidths = new int[] { 200, 400 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }

        Composite buttonsBar = toolkit.createComposite(allSwimlanesComposite);
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
        enableAction(deleteButton, selected.size() == 1);
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

    public void propertyChange(PropertyChangeEvent evt) {
        String type = evt.getPropertyName();
        if (NotificationMessages.NODE_CHILDS_CHANGED.equals(type)) {
            fillViewer();
        } else if (evt.getSource() instanceof Swimlane) {
            if (NotificationMessages.PROPERTY_NAME.equals(type) || NotificationMessages.PROPERTY_CONFIGURATION.equals(type)) {
                tableViewer.refresh(evt.getSource());
            }
        }
    }

    private void fillViewer() {
        List<Swimlane> swimlanes = getDefinition().getSwimlanes();
        tableViewer.setInput(swimlanes);
        for (Swimlane var : swimlanes) {
            var.addPropertyChangeListener(this);
        }
        updateButtons();
    }

    @Override
    public void dispose() {
        for (Swimlane var : getDefinition().getSwimlanes()) {
            var.removePropertyChangeListener(this);
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
        StringBuffer message = new StringBuffer(Messages.getString("Swimlane.UsedInStates")).append("\n");
        StringBuffer stateNames = new StringBuffer();
        for (SwimlanedNode node : getDefinition().getChildren(SwimlanedNode.class)) {
            if (node.getSwimlaneName() != null && swimlane.getName().equals(node.getSwimlaneName())) {
                stateNames.append(" - ").append(node.getName()).append("\n");
            }
        }
        if (stateNames.length() > 0) {
            message.append(stateNames);
        } else {
            message.append(Messages.getString("Swimlane.NotUsed"));
        }
        if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), Messages.getString("ConfirmDelete"), message.toString())) {
            ProcessDefinitionRemoveSwimlaneCommand command = new ProcessDefinitionRemoveSwimlaneCommand();
            command.setProcessDefinition(getDefinition());
            command.setSwimlane(swimlane);
            editor.getCommandStack().execute(command);
            getDefinition().getSwimlaneGUIConfiguration().removeSwimlanePath(swimlane.getName());
        }
    }

    private class SearchSwimlaneUsageSelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                Swimlane swimlane = (Swimlane) selection.getFirstElement();
                GPDSearchQuery query = new GPDSearchQuery(editor.getDefinitionFile(), getDefinition(), swimlane.getName());
                NewSearchUI.runQueryInBackground(query);
            } catch (Exception ex) {
                DesignerLogger.logError(ex);
            }
        }
    }

    private class RenameSwimlaneSelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();

            UpdateSwimlaneNameDialog renameDialog = new UpdateSwimlaneNameDialog(swimlane.getProcessDefinition(), false);
            renameDialog.setName(swimlane.getName());
            int result = renameDialog.open();
            String newName = renameDialog.getName();
            boolean useLtk = renameDialog.isProceedRefactoring();
            if (result != IDialogConstants.OK_ID) {
                return;
            }
            IResource projectRoot = editor.getDefinitionFile().getParent();
            if (useLtk) {
                PortabilityRefactoring ref = new PortabilityRefactoring(editor.getDefinitionFile(), editor.getDefinition(), swimlane.getName(),
                        newName);
                useLtk &= ref.isUserInteractionNeeded();
                if (useLtk) {
                    RenameRefactoringWizard wizard = new RenameRefactoringWizard(ref);
                    wizard.setDefaultPageTitle(Messages.getString("Refactoring.variable.name"));
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
            }
            swimlane.setName(newName);
            if (useLtk) {
                IDE.saveAllEditors(new IResource[] { projectRoot }, false);
            }
        }
    }

    private class RemoveSwimlaneSelectionListener extends SelectionAdapter {

        @SuppressWarnings("unchecked")
        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            List<Swimlane> swimlanes = selection.toList();
            for (Swimlane swimlane : swimlanes) {
                try {
                    delete(swimlane);
                } catch (Exception e1) {
                    DesignerLogger.logError(e1);
                }
            }
        }
    }

    private class CreateSwimlaneSelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            UpdateSwimlaneNameDialog dialog = new UpdateSwimlaneNameDialog(getDefinition(), true);
            if (dialog.open() == IDialogConstants.OK_ID) {
                Swimlane newSwimlane = JpdlVersionRegistry.getElementTypeDefinition(getDefinition().getJpdlVersion(), "swimlane").createElement();
                newSwimlane.setParent(getDefinition());
                newSwimlane.setName(dialog.getName());
                newSwimlane.setDelegationClassName(Swimlane.DELEGATION_CLASS_NAME);
                getDefinition().addSwimlane(newSwimlane);
                tableViewer.setSelection(new StructuredSelection(newSwimlane));
            }
        }

    }

    private class ChangeSwimlaneSelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
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

    private class CopySwimlaneSelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Clipboard.getDefault().setContents(selection.toList());
        }

    }

    private class PasteSwimlaneSelectionListener extends SelectionAdapter {

        @SuppressWarnings("unchecked")
        @Override
        public void widgetSelected(SelectionEvent e) {
            List<Swimlane> newSwimlanes = (List<Swimlane>) Clipboard.getDefault().getContents();
            for (Swimlane swimlane : newSwimlanes) {
                boolean add = false;
                Swimlane newSwimlane = getDefinition().getSwimlaneByName(swimlane.getName());
                if (newSwimlane == null) {
                    newSwimlane = JpdlVersionRegistry.getElementTypeDefinition(getDefinition().getJpdlVersion(), "swimlane").createElement();
                    newSwimlane.setParent(getDefinition());
                    newSwimlane.setName(swimlane.getName());
                    add = true;
                }
                newSwimlane.setDelegationClassName(swimlane.getDelegationClassName());
                newSwimlane.setDelegationConfiguration(swimlane.getDelegationConfiguration());
                if (add) {
                    getDefinition().addSwimlane(newSwimlane);
                }
            }
        }

    }

    private static class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {

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

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

}
