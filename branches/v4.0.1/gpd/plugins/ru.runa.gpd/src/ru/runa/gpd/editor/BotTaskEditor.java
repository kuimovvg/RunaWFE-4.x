package ru.runa.gpd.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.extension.handler.ParamDefGroup;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.BotTaskType;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.ui.custom.XmlHighlightTextStyling;
import ru.runa.gpd.ui.dialog.ChooseHandlerClassDialog;
import ru.runa.gpd.ui.dialog.InfoWithDetailsDialog;
import ru.runa.gpd.ui.wizard.BotTaskParamDefWizard;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.collect.Maps;

public class BotTaskEditor extends EditorPart implements ISelectionListener, IResourceChangeListener, PropertyChangeListener {
    public static final String ID = "ru.runa.gpd.editor.BotTaskEditor";
    private BotTask botTask;
    private boolean dirty;
    private IFile botTaskFile;
    private boolean extendedMode;
    private Composite editorComposite;
    private Text handlerText;
    private Button chooseTaskHandlerClassButton;
    private Button editConfigurationButton;
    private StyledText configurationText;
    private TableViewer inputParamTableViewer;
    private TableViewer outputParamTableViewer;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        FileEditorInput fileInput = (FileEditorInput) input;
        botTaskFile = fileInput.getFile();
        try {
            botTask = BotCache.getBotTaskNotNull(botTaskFile);
        } catch (Exception e) {
            throw new PartInitException("", e);
        }
        if (botTask.getType() != BotTaskType.SIMPLE) {
            this.setTitleImage(SharedImages.getImage("icons/bot_task_formal.gif"));
        }
        setPartName(botTask.getName());
        setDirty(false);
        getSite().getPage().addSelectionListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    public void setExtendedMode(boolean extendedMode) {
        this.extendedMode = extendedMode;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            WorkspaceOperations.saveBotTask(botTaskFile, botTask);
            setDirty(false);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void dispose() {
        // If bot task has been changed but not saved we should reload it from XML
        if (isDirty()) {
            try {
                BotCache.invalidateBotTask(botTaskFile, botTask);
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
        super.dispose();
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        editorComposite = new Composite(parent, SWT.NONE);
        editorComposite.setLayout(new GridLayout());
        editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        rebuildView(editorComposite);
    }

    private void rebuildView(Composite composite) {
        for (Control control : composite.getChildren()) {
            control.dispose();
        }
        createTaskHandlerClassField(composite);
        if (botTask.getType() == BotTaskType.PARAMETERIZED) {
            createConfTableViewer(composite, ParamDefGroup.NAME_INPUT);
            createConfTableViewer(composite, ParamDefGroup.NAME_OUTPUT);
        } else if (botTask.getType() == BotTaskType.EXTENDED) {
            ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.BORDER);
            scrolledComposite.setExpandHorizontal(true);
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            Composite innerComposite = new Composite(scrolledComposite, SWT.NONE);
            innerComposite.setLayout(new GridLayout());
            createConfTableViewer(innerComposite, ParamDefGroup.NAME_INPUT);
            createConfTableViewer(innerComposite, ParamDefGroup.NAME_OUTPUT);
            createConfigurationFields(innerComposite);
            createConfigurationArea(innerComposite);
            scrolledComposite.setMinSize(SWT.DEFAULT, 700);
            scrolledComposite.setContent(innerComposite);
        } else {
            createConfigurationFields(composite);
            createConfigurationArea(composite);
        }
        populateFields();
        composite.layout(true, true);
    }

    private void createTaskHandlerClassField(final Composite parent) {
        Composite dynaComposite = new Composite(parent, SWT.NONE);
        dynaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dynaComposite.setLayout(new GridLayout(3, false));
        Label label = new Label(dynaComposite, SWT.NONE);
        GridData gridData = new GridData(GridData.BEGINNING);
        gridData.widthHint = 150;
        label.setLayoutData(gridData);
        label.setText(Localization.getString("BotTaskEditor.taskHandler"));
        handlerText = new Text(dynaComposite, SWT.BORDER);
        handlerText.setEditable(false);
        handlerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        chooseTaskHandlerClassButton = new Button(dynaComposite, SWT.NONE);
        chooseTaskHandlerClassButton.setText(Localization.getString("button.choose"));
        chooseTaskHandlerClassButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    ChooseHandlerClassDialog dialog = new ChooseHandlerClassDialog(HandlerArtifact.TASK_HANDLER);
                    String className = dialog.openDialog();
                    if (className != null) {
                        boolean taskHandlerParameterized = BotTaskUtils.isTaskHandlerParameterized(className);
                        if (extendedMode && taskHandlerParameterized) {
                            InfoWithDetailsDialog.open(Localization.getString("message.warning"),
                                    Localization.getString("BotTaskEditor.parameterizedTaskInExtendedModeNotAllowed"), "");
                            return;
                        }
                        handlerText.setText(className);
                        botTask.setDelegationClassName(className);
                        // botTask.setDelegationConfiguration("");
                        if (taskHandlerParameterized) {
                            DelegableProvider provider = HandlerRegistry.getProvider(className);
                            String xml = XmlUtil.getParamDefConfig(provider.getBundle(), className);
                            botTask.setParamDefConfig(ParamDefConfig.parse(xml));
                            botTask.setType(BotTaskType.PARAMETERIZED);
                        }
                        setDirty(true);
                        rebuildView(parent);
                    }
                } catch (Throwable th) {
                    PluginLogger.logError(th);
                }
            }
        });
    }

    private void createConfigurationFields(Composite parent) {
        Composite dynaComposite = new Composite(parent, SWT.NONE);
        dynaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dynaComposite.setLayout(new GridLayout(2, false));
        GridData gridData;
        Label label = new Label(dynaComposite, SWT.NONE);
        gridData = new GridData(GridData.BEGINNING);
        gridData.widthHint = 150;
        label.setLayoutData(gridData);
        label.setText(Localization.getString("BotTaskEditor.configuration"));
        editConfigurationButton = new Button(dynaComposite, SWT.NONE);
        editConfigurationButton.setText(Localization.getString("button.change"));
        // TODO add SafeSelectionAdapter; SafeAction with error handling
        editConfigurationButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Map<String, String> variables = Maps.newHashMap();
                if (botTask.getParamDefConfig() != null) {
                    for (ParamDefGroup paramDefGroup : botTask.getParamDefConfig().getGroups()) {
                        for (ParamDef paramDef : paramDefGroup.getParameters()) {
                            if (paramDef.getFormatFilters().size() > 0) {
                                variables.put(paramDef.getName(), paramDef.getFormatFilters().get(0));
                            }
                        }
                    }
                }
                DelegableProvider provider = HandlerRegistry.getProvider(botTask.getDelegationClassName());
                String newConfiguration = provider.showConfigurationDialog(botTask.getDelegationConfiguration(), variables);
                if (newConfiguration != null) {
                    configurationText.setText(newConfiguration);
                    botTask.setDelegationConfiguration(newConfiguration);
                    setDirty(true);
                }
            }
        });
        editConfigurationButton.setEnabled(botTask.getDelegationClassName() != null);
    }

    private void createConfigurationArea(Composite parent) {
        Composite dynaComposite = new Composite(parent, SWT.NONE);
        dynaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        dynaComposite.setLayout(new GridLayout());
        configurationText = new StyledText(dynaComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        configurationText.setLineSpacing(2);
        configurationText.setEditable(false);
        configurationText.setLayoutData(new GridData(GridData.FILL_BOTH));
        if (botTask.isDelegationConfigurationInXml()) {
            configurationText.addLineStyleListener(new XmlHighlightTextStyling());
        }
    }

    private void createConfTableViewer(Composite parent, final String parameterType) {
        Composite dynaConfComposite = new Composite(parent, SWT.NONE);
        dynaConfComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dynaConfComposite.setLayout(new GridLayout());
        Label descriptionLabel = new Label(dynaConfComposite, SWT.NONE);
        if (ParamDefGroup.NAME_INPUT.equals(parameterType)) {
            descriptionLabel.setText(Localization.getString("ParamDefGroup.group.input"));
        } else {
            descriptionLabel.setText(Localization.getString("ParamDefGroup.group.output"));
        }
        descriptionLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        TableViewer confTableViewer;
        if (ParamDefGroup.NAME_INPUT.equals(parameterType)) {
            inputParamTableViewer = new TableViewer(dynaConfComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
            confTableViewer = inputParamTableViewer;
        } else {
            outputParamTableViewer = new TableViewer(dynaConfComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
            confTableViewer = outputParamTableViewer;
        }
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 120;
        confTableViewer.getControl().setLayoutData(gridData);
        Table table = confTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("BotTaskEditor.name"), Localization.getString("BotTaskEditor.type") };
        int[] columnWidths = new int[] { 400, 300 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }
        confTableViewer.setLabelProvider(new TableLabelProvider());
        confTableViewer.setContentProvider(new ArrayContentProvider());
        setTableInput(parameterType);
        Composite buttonArea = new Composite(dynaConfComposite, SWT.NONE);
        buttonArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttonArea.setLayout(new GridLayout(3, false));
        Button addedParamDefButton = new Button(buttonArea, SWT.NONE);
        addedParamDefButton.setText(Localization.getString("button.add"));
        addedParamDefButton.setEnabled(botTask.getType() != BotTaskType.PARAMETERIZED);
        addedParamDefButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                    if (parameterType.equals(group.getName())) {
                        BotTaskParamDefWizard wizard = new BotTaskParamDefWizard(group, null);
                        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
                        if (dialog.open() == Window.OK) {
                            setTableInput(parameterType);
                            setDirty(true);
                        }
                    }
                }
            }
        });
        final Button editParamDefButton = new Button(buttonArea, SWT.NONE);
        editParamDefButton.setText(Localization.getString("button.edit"));
        editParamDefButton.setEnabled(botTask.getType() != BotTaskType.PARAMETERIZED
                && ((IStructuredSelection) getParamTableViewer(parameterType).getSelection()).getFirstElement() != null);
        editParamDefButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                    if (parameterType.equals(group.getName())) {
                        IStructuredSelection selection = (IStructuredSelection) getParamTableViewer(parameterType).getSelection();
                        String[] row = (String[]) selection.getFirstElement();
                        if (row == null) {
                            return;
                        }
                        for (ParamDef paramDef : group.getParameters()) {
                            if (paramDef.getName().equals(row[0])) {
                                BotTaskParamDefWizard wizard = new BotTaskParamDefWizard(group, paramDef);
                                WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
                                if (dialog.open() == Window.OK) {
                                    setTableInput(parameterType);
                                    setDirty(true);
                                }
                            }
                        }
                    }
                }
            }
        });
        final Button deleteParamDefButton = new Button(buttonArea, SWT.NONE);
        deleteParamDefButton.setText(Localization.getString("button.delete"));
        deleteParamDefButton.setEnabled(botTask.getType() != BotTaskType.PARAMETERIZED
                && ((IStructuredSelection) getParamTableViewer(parameterType).getSelection()).getFirstElement() != null);
        deleteParamDefButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                    if (parameterType.equals(group.getName())) {
                        IStructuredSelection selection = (IStructuredSelection) getParamTableViewer(parameterType).getSelection();
                        String[] row = (String[]) selection.getFirstElement();
                        if (row == null) {
                            return;
                        }
                        for (ParamDef paramDef : group.getParameters()) {
                            if (paramDef.getName().equals(row[0])) {
                                group.getParameters().remove(paramDef);
                                setTableInput(parameterType);
                                setDirty(true);
                                break;
                            }
                        }
                    }
                }
            }
        });
        confTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                editParamDefButton.setEnabled(botTask.getType() != BotTaskType.PARAMETERIZED && selection.getFirstElement() != null);
                deleteParamDefButton.setEnabled(botTask.getType() != BotTaskType.PARAMETERIZED && selection.getFirstElement() != null);
            }
        });
    }

    private void setTableInput(String groupType) {
        TableViewer confTableViewer = getParamTableViewer(groupType);
        List<ParamDef> paramDefs = new ArrayList<ParamDef>();
        for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
            if (groupType.equals(group.getName())) {
                paramDefs.addAll(group.getParameters());
            }
        }
        List<String[]> input = new ArrayList<String[]>(paramDefs.size());
        for (ParamDef paramDef : paramDefs) {
            String label = "";
            if (paramDef.getFormatFilters().size() > 0) {
                String type = paramDef.getFormatFilters().get(0);
                label = VariableFormatRegistry.getInstance().getArtifactNotNullByJavaClassName(type).getLabel();
            }
            input.add(new String[] { paramDef.getName(), label });
        }
        confTableViewer.setInput(input);
    }

    private TableViewer getParamTableViewer(String parameterType) {
        if (ParamDefGroup.NAME_INPUT.equals(parameterType)) {
            return inputParamTableViewer;
        } else {
            return outputParamTableViewer;
        }
    }

    private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            String[] data = (String[]) element;
            return data[index];
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    private void populateFields() {
        handlerText.setText(botTask.getDelegationClassName());
        if (botTask.getType() != BotTaskType.PARAMETERIZED) {
            configurationText.setText(botTask.getDelegationConfiguration());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyNames.PROPERTY_DIRTY.equals(evt.getPropertyName())) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        EditorUtils.closeEditorIfRequired(event, botTaskFile, this);
    }
}
