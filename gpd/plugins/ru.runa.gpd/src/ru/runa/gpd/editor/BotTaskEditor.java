package ru.runa.gpd.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
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

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.handler.DelegableConfigurationDialog;
import ru.runa.gpd.handler.DelegableProvider;
import ru.runa.gpd.handler.HandlerArtifact;
import ru.runa.gpd.handler.HandlerRegistry;
import ru.runa.gpd.handler.action.ParamDef;
import ru.runa.gpd.handler.action.ParamDefConfig;
import ru.runa.gpd.handler.action.ParamDefGroup;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.ui.dialog.ChooseHandlerClassDialog;
import ru.runa.gpd.ui.wizard.ParamDefWizard;
import ru.runa.gpd.util.BotTaskContentUtil;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;

public class BotTaskEditor extends EditorPart implements ISelectionListener, IResourceChangeListener, PropertyChangeListener {
    public static final String ID = "ru.runa.gpd.editor.BotTaskEditor";
    private BotTask task;
    private IFile taskInfoFile;
    private boolean parameterizedTask;
    private boolean initParameterizedTask;
    private boolean configParamInPlugin;
    private Composite editorComposite;
    private Text handlerText;
    private Button chooseBotButton;
    private Button editConfButton;
    private StyledText configurationText;
    private TableViewer inputParamTableViewer;
    private TableViewer outputParamTableViewer;

    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            if (!BotTaskConfigHelper.isParamDefConfigEmpty(task.getParamDefConfig()) && configurationText != null && !configurationText.isDisposed()) {
                task.setConfig(configurationText.getText());
            }
            WorkspaceOperations.saveBotTask(taskInfoFile, task);
            task = BotTaskContentUtil.getBotTaskFromFile(taskInfoFile);
            task.setDirty(false);
            firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        FileEditorInput fileInput = (FileEditorInput) input;
        taskInfoFile = fileInput.getFile();
        task = BotTaskContentUtil.getBotTaskFromFile(taskInfoFile);
        configParamInPlugin = BotTaskConfigHelper.isTaskConfigurationInPlugin(task.getClazz());
        parameterizedTask = !BotTaskConfigHelper.isParamDefConfigEmpty(task.getParamDefConfig());
        initParameterizedTask = parameterizedTask;
        if (parameterizedTask) {
            this.setTitleImage(SharedImages.getImage("icons/bot_task_formal.gif"));
        }
        setPartName(task.getName());
        task.setDirty(false);
        getSite().getPage().addSelectionListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        //        try { TODO need?
        //            IJavaProject project = ProjectFinder.getAnyJavaProject();
        //            if (project != null) {
        //                CustomizationRegistry.init(project);
        //            }
        //        } catch (Exception e) {
        //            DesignerLogger.logError("Exception while loading customization ...", e);
        //        }
    }

    @Override
    public boolean isDirty() {
        return task.isDirty();
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        editorComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 3;
        editorComposite.setLayout(layout);
        editorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buildTaskConfiguration(editorComposite);
    }

    private void buildTaskConfiguration(Composite composite) {
        for (Control control : composite.getChildren()) {
            control.dispose();
        }
        createHandlerField(composite);
        if (parameterizedTask) {
            if (configParamInPlugin) {
                createConfTableViewer(composite, ParamDefGroup.NAME_INPUT);
                createConfTableViewer(composite, ParamDefGroup.NAME_OUTPUT);
            } else {
                ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.BORDER);
                scrolledComposite.setExpandHorizontal(true);
                scrolledComposite.setExpandVertical(true);
                scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
                Composite innerComposite = new Composite(scrolledComposite, SWT.NONE);
                innerComposite.setLayout(new GridLayout(3, false));
                createConfTableViewer(innerComposite, ParamDefGroup.NAME_INPUT);
                createConfTableViewer(innerComposite, ParamDefGroup.NAME_OUTPUT);
                createConfigurationFileds(innerComposite);
                createConfigurationArea(innerComposite);
                scrolledComposite.setMinSize(SWT.DEFAULT, 700);
                scrolledComposite.setContent(innerComposite);
            }
        } else {
            createConfigurationFileds(composite);
            createConfigurationArea(composite);
        }
        populateFields();
        composite.layout(true, true);
    }

    private void createHandlerField(final Composite parent) {
        Composite dynaComposite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.CENTER);
        data.horizontalSpan = 3;
        data.minimumHeight = 30;
        dynaComposite.setLayoutData(data);
        dynaComposite.setLayout(new GridLayout(3, false));
        GridData gridData;
        Label label = new Label(dynaComposite, SWT.NONE);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 100;
        label.setLayoutData(gridData);
        label.setText(Localization.getString("BotTaskEditor.taskHandler"));
        handlerText = new Text(dynaComposite, SWT.BORDER);
        handlerText.setEditable(false);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 300;
        handlerText.setLayoutData(gridData);
        chooseBotButton = new Button(dynaComposite, SWT.NONE);
        chooseBotButton.setText(Localization.getString("BotTaskEditor.choose"));
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 150;
        chooseBotButton.setLayoutData(gridData);
        chooseBotButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ChooseHandlerClassDialog dialog = new ChooseHandlerClassDialog(HandlerArtifact.TASK_HANDLER);
                String className = dialog.openDialog();
                if (className != null) {
                    configParamInPlugin = false;
                    parameterizedTask = initParameterizedTask;
                    handlerText.setText(className);
                    task.setClazz(className);
                    task.setDelegationClassName(className);
                    task.setConfig("");
                    task.setDelegationConfiguration("");
                    if (BotTaskConfigHelper.isTaskConfigurationInPlugin(task.getDelegationClassName())) {
                        DelegableProvider provider = HandlerRegistry.getProvider(className);
                        try {
                            String path = "/conf/" + getSimpleClassName(className) + ".xml";
                            InputStream is = provider.getBundle().getEntry(path).openStream();
                            String conf = IOUtils.readStream(is);
                            Document doc = DocumentHelper.parseText(conf);
                            task.setParamDefConfig(ParamDefConfig.parse(doc));
                            configParamInPlugin = true;
                            parameterizedTask = true;
                            task.setConfig(conf);
                            task.setDelegationConfiguration(conf);
                            configurationText.setText(conf);
                        } catch (Exception e1) {
                            PluginLogger.logError("Exception while loading plugin configuration ...", e1);
                        }
                    }
                    task.setDirty(true);
                    firePropertyChange(IEditorPart.PROP_DIRTY);
                    buildTaskConfiguration(parent);
                }
            }
        });
    }

    private String getSimpleClassName(String className) {
        int dotIndex = className.lastIndexOf(".");
        return className.substring(dotIndex + 1);
    }

    private void createConfigurationFileds(Composite parent) {
        Composite dynaComposite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.CENTER);
        data.horizontalSpan = 2;
        data.minimumHeight = 30;
        dynaComposite.setLayoutData(data);
        dynaComposite.setLayout(new GridLayout(2, false));
        GridData gridData;
        Label label = new Label(dynaComposite, SWT.NONE);
        gridData = new GridData(GridData.BEGINNING);
        gridData.minimumWidth = 100;
        label.setLayoutData(gridData);
        label.setText(Localization.getString("BotTaskEditor.configuration"));
        editConfButton = new Button(dynaComposite, SWT.NONE);
        editConfButton.setText(Localization.getString("BotTaskEditor.change"));
        gridData = new GridData(GridData.BEGINNING);
        gridData.minimumWidth = 100;
        editConfButton.setLayoutData(gridData);
        editConfButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (task.getDelegationClassName() != null) {
                    DelegableProvider provider = HandlerRegistry.getProvider(task.getDelegationClassName());
                    if (provider.getBundle() != null && !BotTaskConfigHelper.isTaskConfigurationInPlugin(task.getDelegationClassName())) {
                        String newConfig = provider.showConfigurationDialog(task);
                        if (newConfig != null) {
                            configurationText.setText(newConfig);
                            task.setConfig(configurationText.getText());
                            task.setDirty(true);
                            firePropertyChange(IEditorPart.PROP_DIRTY);
                        }
                        return;
                    }
                }
                DelegableConfigurationDialog dialog = new DelegableConfigurationDialog(task.getConfig() != null ? task.getConfig() : "");
                if (dialog.open() == Window.OK) {
                    configurationText.setText(dialog.getResult());
                    task.setConfig(configurationText.getText());
                    task.setDirty(true);
                    firePropertyChange(IEditorPart.PROP_DIRTY);
                }
            }
        });
        String delegationClassName = task.getDelegationClassName();
        if (delegationClassName != null) {
            DelegableProvider provider = HandlerRegistry.getProvider(delegationClassName);
            editConfButton.setEnabled(provider.getBundle() == null || !BotTaskConfigHelper.isTaskConfigurationInPlugin(delegationClassName));
        }
    }

    private void createConfigurationArea(Composite parent) {
        Composite dynaComposite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.CENTER | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        data.minimumHeight = 150;
        dynaComposite.setLayoutData(data);
        dynaComposite.setLayout(new GridLayout(2, false));
        configurationText = new StyledText(dynaComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        configurationText.setLineSpacing(2);
        configurationText.setEditable(false);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        gridData.heightHint = 150;
        gridData.minimumWidth = 800;
        configurationText.setLayoutData(gridData);
    }

    private void createConfTableViewer(Composite parent, final String parameterType) {
        Composite dynaConfComposite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.CENTER | GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        data.minimumHeight = 120;
        data.minimumWidth = 800;
        dynaConfComposite.setLayoutData(data);
        dynaConfComposite.setLayout(new GridLayout(2, false));
        Label descriptionLabel = new Label(dynaConfComposite, SWT.NONE);
        if (ParamDefGroup.NAME_INPUT.equals(parameterType)) {
            descriptionLabel.setText(Localization.getString("BotTaskEditor.inputParam"));
        } else {
            descriptionLabel.setText(Localization.getString("BotTaskEditor.outputParam"));
        }
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.horizontalSpan = 3;
        descriptionLabel.setLayoutData(data);
        TableViewer confTableViewer;
        if (ParamDefGroup.NAME_INPUT.equals(parameterType)) {
            inputParamTableViewer = new TableViewer(dynaConfComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
            confTableViewer = inputParamTableViewer;
        } else {
            outputParamTableViewer = new TableViewer(dynaConfComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
            confTableViewer = outputParamTableViewer;
        }
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        data.heightHint = 120;
        confTableViewer.getControl().setLayoutData(data);
        Table table = confTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("BotTaskEditor.name"), Localization.getString("BotTaskEditor.label"),
                Localization.getString("BotTaskEditor.type"), Localization.getString("BotTaskEditor.defaultValue"), Localization.getString("BotTaskEditor.required") };
        int[] columnWidths = new int[] { 200, 200, 150, 200, 150 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }
        confTableViewer.setLabelProvider(new TableLabelProvider());
        confTableViewer.setContentProvider(new ArrayContentProvider());
        setTableInput(parameterType);
        Composite buttonArea = new Composite(dynaConfComposite, SWT.NONE);
        data = new GridData(GridData.CENTER | GridData.FILL_HORIZONTAL);
        buttonArea.setLayoutData(data);
        buttonArea.setLayout(new GridLayout(3, false));
        Button addedParamDefButton = new Button(buttonArea, SWT.NONE);
        addedParamDefButton.setText(Localization.getString("BotTaskEditor.add"));
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.minimumWidth = 150;
        addedParamDefButton.setEnabled(!configParamInPlugin);
        addedParamDefButton.setLayoutData(gridData);
        addedParamDefButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ParamDefGroup group : task.getParamDefConfig().getGroups()) {
                    if (parameterType.equals(group.getName())) {
                        ParamDefWizard wizard = new ParamDefWizard(group, null);
                        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
                        if (dialog.open() == Window.OK) {
                            setTableInput(parameterType);
                            task.setDirty(true);
                            firePropertyChange(IEditorPart.PROP_DIRTY);
                        }
                    }
                }
            }
        });
        final Button editParamDefButton = new Button(buttonArea, SWT.NONE);
        editParamDefButton.setText(Localization.getString("BotTaskEditor.edit"));
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.minimumWidth = 150;
        editParamDefButton.setLayoutData(gridData);
        editParamDefButton.setEnabled(!configParamInPlugin && ((IStructuredSelection) getParamTableViewer(parameterType).getSelection()).getFirstElement() != null);
        editParamDefButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ParamDefGroup group : task.getParamDefConfig().getGroups()) {
                    if (parameterType.equals(group.getName())) {
                        IStructuredSelection selection = (IStructuredSelection) getParamTableViewer(parameterType).getSelection();
                        String[] row = (String[]) selection.getFirstElement();
                        if (row == null) {
                            return;
                        }
                        for (ParamDef paramDef : group.getParameters()) {
                            if (paramDef.getName().equals(row[0])) {
                                ParamDefWizard wizard = new ParamDefWizard(group, paramDef);
                                WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
                                if (dialog.open() == Window.OK) {
                                    setTableInput(parameterType);
                                    task.setDirty(true);
                                    firePropertyChange(IEditorPart.PROP_DIRTY);
                                }
                            }
                        }
                    }
                }
            }
        });
        final Button deleteParamDefButton = new Button(buttonArea, SWT.NONE);
        deleteParamDefButton.setText(Localization.getString("BotTaskEditor.remove"));
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.minimumWidth = 150;
        deleteParamDefButton.setLayoutData(gridData);
        deleteParamDefButton.setEnabled(!configParamInPlugin && ((IStructuredSelection) getParamTableViewer(parameterType).getSelection()).getFirstElement() != null);
        deleteParamDefButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ParamDefGroup group : task.getParamDefConfig().getGroups()) {
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
                                task.setDirty(true);
                                firePropertyChange(IEditorPart.PROP_DIRTY);
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
                editParamDefButton.setEnabled(!configParamInPlugin && selection.getFirstElement() != null);
                deleteParamDefButton.setEnabled(!configParamInPlugin && selection.getFirstElement() != null);
            }
        });
    }

    private void setTableInput(String parameterType) {
        TableViewer confTableViewer = getParamTableViewer(parameterType);
        List<ParamDef> paramDefs = new ArrayList<ParamDef>();
        for (ParamDefGroup group : task.getParamDefConfig().getGroups()) {
            if (parameterType.equals(group.getName())) {
                paramDefs.addAll(group.getParameters());
            }
        }
        List<String[]> input = new ArrayList<String[]>(paramDefs.size());
        for (ParamDef paramDef : paramDefs) {
            String[] filters = paramDef.getFormatFilters().toArray(new String[0]);
            String defFormat = "";
            if (filters.length > 0) {
                defFormat = filters[0];
            }
            input.add(new String[] { paramDef.getName(), paramDef.getLabel(), defFormat, paramDef.getDefaultValue(), "" + paramDef.isOptional() });
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
        handlerText.setText(task.getClazz() != null ? task.getClazz() : "");
        if (configurationText != null && !configurationText.isDisposed()) {
            configurationText.setText(task.getConfig() != null ? task.getConfig() : "");
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
        EditorUtils.closeEditorIfRequired(event, taskInfoFile, this);
    }

    public void setParameterized(boolean parameterized) {
        this.parameterizedTask = parameterized;
        this.initParameterizedTask = parameterized;
        buildTaskConfiguration(editorComposite);
    }
}
