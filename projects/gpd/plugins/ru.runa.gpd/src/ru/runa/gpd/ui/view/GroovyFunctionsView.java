package ru.runa.gpd.ui.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ru.cg.runaex.shared.bean.project.xml.GroovyFunction;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.handler.GroovyActionHandlerProvider;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.ProjectFinder;

public class GroovyFunctionsView extends ViewPart implements ISelectionListener, IPartListener2 {
    private TableViewer viewer;
    private FormToolkit toolkit;
    private Form form;
    private IProject currentProject;
    private boolean visible;
    private Action addAction;
    private Action deleteAction;

    private final String CODE = "Code";
    private final String DESCRIPTION = "Description";

    private String[] columnNames = new String[] { CODE, DESCRIPTION };

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
        getSite().getWorkbenchWindow().getPartService().addPartListener(this);
    }

    @Override
    public void dispose() {
        getSite().getWorkbenchWindow().getPartService().removePartListener(this);
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        super.dispose();
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {

    }

    private void createTable(Composite parent) {
        final Table table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 400;
        table.setLayoutData(data);

        TableColumn column = new TableColumn(table, SWT.FILL, 0);
        column.setText(Localization.getString("functionCode"));
        column.setWidth(400);

        column = new TableColumn(table, SWT.FILL, 1);
        column.setText(Localization.getString("functionDescription"));
        column.setWidth(400);

        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                deleteAction.setEnabled(currentProject != null && getSelectedFunction() != null);
            }
        });

        viewer = new TableViewer(table) {
            protected void triggerEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                super.triggerEditorActivationEvent(event);
                addAction.setEnabled(false);
            }

            protected void applyEditorValue() {
                super.applyEditorValue();
                if (getSelectedFunction() != null) {
                    String selectedFunctionCode = getSelectedFunction().getCode();
                    String code = StringUtils.trimToNull(selectedFunctionCode);
                    if (code == null) {
                        removeSelectedFunction(false);
                    }
                }
                addAction.setEnabled(true);
            }
        };
        viewer.setUseHashlookup(true);
        viewer.setColumnProperties(columnNames);

        CellEditor[] editors = new CellEditor[2];
        editors[0] = new GroovyCodePropertyCellEditor(table);
        editors[1] = new TextCellEditor(table);

        viewer.setCellEditors(editors);
        viewer.setCellModifier(new GroovyFunctionCellModifier(this, columnNames, viewer));
        viewer.setContentProvider(new GroovyFunctionsViewContentProvider(currentProject));
        viewer.setLabelProvider(new GroovyFunctionsViewLabelProvider());
        viewer.setInput(new Object());
    }

    private GroovyFunction getSelectedFunction() {
        GroovyFunction function = null;
        IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
        if (sel != null) {
            function = (GroovyFunction) sel.getFirstElement();
        }
        return function;
    }

    private void removeSelectedFunction(boolean refresh) {
        GroovyFunction function = getSelectedFunction();

        if (function != null) {
            deleteAction.setEnabled(false);
            getProvider().removeFromList(function);
            if (refresh) {
                refreshView();
            }
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        form = toolkit.createForm(parent);
        GridLayout layout = new GridLayout();
        form.getBody().setLayout(layout);
        toolkit.decorateFormHeading(form);

        addAction = new Action("", SharedImages.getImageDescriptor("icons/add_function.png")) {
            @Override
            public void run() {
                GroovyFunction function = new GroovyFunction("", "");
                viewer.add(function);
                getProvider().saveToList(function);
                deleteAction.setEnabled(true);
                viewer.editElement(function, 0);
            }
        };
        addAction.setToolTipText(Localization.getString("add_function"));

        deleteAction = new Action("", SharedImages.getImageDescriptor("icons/delete_function.png")) {
            @Override
            public void run() {
                removeSelectedFunction(true);
            }
        };
        deleteAction.setToolTipText(Localization.getString("delete_function"));

        deleteAction.setEnabled(false);

        form.getToolBarManager().add(addAction);
        form.getToolBarManager().add(deleteAction);
        form.getToolBarManager().update(true);
        form.setToolBarVerticalAlignment(SWT.BOTTOM);

        form.getBody().setLayoutData(new GridData(GridData.FILL_BOTH));
        createTable(form.getBody());
        getSite().setSelectionProvider(viewer);
        MenuManager menuMgr = new MenuManager();
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                GroovyFunctionsView.this.fillContextMenu(manager);
            }
        });
        viewer.getControl().setMenu(menu);
    }

    private void refreshView() {
        if (!viewer.getControl().isDisposed()) {
            viewer.refresh();
        }
    }

    protected void fillContextMenu(IMenuManager manager) {
    }

    @Override
    public void setFocus() {
    }

    private boolean isThisPart(IWorkbenchPartReference ref) {
        IWorkbenchPart part = ref.getPart(false);
        return part != null && part.equals(this);
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        IEditorPart actEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

        if (actEditor == null) {
            currentProject = null;
        } else {
            IFile formFile = (IFile) (((IFileEditorInput) (actEditor.getEditorInput())).getFile());
            currentProject = formFile.getProject();
        }
        if (visible) {
            refreshProviderIfNeeded();
            addAction.setEnabled(currentProject != null);
            GroovyFunction function = getSelectedFunction();
            deleteAction.setEnabled(currentProject != null && function != null);
        }
    }

    private void refreshProviderIfNeeded() {
        GroovyFunctionsViewContentProvider provider = ((GroovyFunctionsViewContentProvider) viewer.getContentProvider());

        IProject project = provider.getProject();
        if (currentProject == null || !currentProject.equals(project)) {
            provider.setProject(currentProject);
            refreshView();
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {

    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        if (isThisPart(partRef)) {
            visible = false;
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {

    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        if (isThisPart(partRef)) {
            visible = false;
        }
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {

    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {

    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        if (isThisPart(partRef)) {
            visible = true;
        }
    }

    public IProject getCurrentProject() {
        return currentProject;
    }

    private static class GroovyFunctionCellModifier implements ICellModifier {
        private TableViewer viewer;
        private String[] columnNames;
        private GroovyFunctionsView view;

        private GroovyFunctionCellModifier(GroovyFunctionsView view, String[] columnNames, TableViewer viewer) {
            this.columnNames = columnNames;
            this.viewer = viewer;
            this.view = view;
        }

        @Override
        public boolean canModify(Object element, String property) {
            return view.getCurrentProject() != null;
        }

        @Override
        public Object getValue(Object element, String property) {
            int columnIndex = Arrays.asList(columnNames).indexOf(property);
            GroovyFunction function = (GroovyFunction) element;

            switch (columnIndex) {
            case 0:
                return function.getCode();
            case 1:
                return function.getDescription();
            }
            return null;
        }

        @Override
        public void modify(Object element, String property, Object value) {
            int columnIndex = Arrays.asList(columnNames).indexOf(property);

            IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
            GroovyFunction function = (GroovyFunction) sel.getFirstElement();

            switch (columnIndex) {
            case 0:
                function.setCode(((String) value).trim());
                break;
            case 1:
                function.setDescription(((String) value).trim());
                break;
            }

            viewer.update(function, null);
            ((GroovyFunctionsViewContentProvider) viewer.getContentProvider()).saveToList(function);
            if (!viewer.getControl().isDisposed()) {
                viewer.refresh();
            }
        }

    }

    private GroovyFunctionsViewContentProvider getProvider() {
        return ((GroovyFunctionsViewContentProvider) viewer.getContentProvider());
    }

    private static class GroovyCodePropertyCellEditor extends DialogCellEditor {

        public GroovyCodePropertyCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected String openDialogBox(Control cellEditorWindow) {
            String delegationConfiguration = (String) this.getValue();
            IFile adjacentFile = ProjectFinder.getCurrentFile();
            ProcessDefinition currentDefinition = null;
            if (adjacentFile != null && adjacentFile.getParent().exists()) {
                IFile definitionFile = ProjectFinder.getProcessDefinitionFile((IFolder) adjacentFile.getParent());
                if (definitionFile != null && definitionFile.exists()) {
                    currentDefinition = ProcessCache.getProcessDefinition(definitionFile);
                }
            }
            List<Variable> variables = currentDefinition != null ? currentDefinition.getVariables(true) : new ArrayList<Variable>();
            delegationConfiguration = StringEscapeUtils.unescapeXml(delegationConfiguration);
            GroovyFunctionConfigurationDilog dialog = new GroovyFunctionConfigurationDilog(delegationConfiguration, variables);
            dialog.open();
            String result = null;
            switch (dialog.getReturnCode()) {
            case Window.OK: {
                result = dialog.getResult();
                break;
            }
            case Window.CANCEL: {
                // Do nothing
                break;
            }
            }
            return result;
        }

        private class GroovyFunctionConfigurationDilog extends GroovyActionHandlerProvider.ConfigurationDialog {
            private final Pattern FUNCTION_PATTERN = Pattern.compile("def\\s+?[^,;()]+(?:(?:\\(\\))|(\\((?:[^,;()]+?\\s*,\\s*)*?[^,;()]+?\\)))\\s*?\\{.*?\\}\\s*",Pattern.DOTALL);

            public GroovyFunctionConfigurationDilog(String initialValue, List<Variable> variables) {
                super(initialValue, variables);
            }

            @Override
            protected void okPressed() {
                String result = assembleResult();
                Matcher matcher = FUNCTION_PATTERN.matcher(result);
                if (matcher.matches()) {
                    super.okPressed();
                } else {
                    MessageBox messageBox = new MessageBox(GroovyFunctionConfigurationDilog.this.getShell(), SWT.ICON_ERROR | SWT.YES);
                    messageBox.setText(Localization.getString("error"));
                    messageBox.setMessage(Localization.getString("functionIsNotMatchPattern"));
                    messageBox.open();
                }
            }

        }
    }

}
