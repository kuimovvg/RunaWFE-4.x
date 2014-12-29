package ru.runa.gpd.formeditor.wysiwyg;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil;
import ru.runa.gpd.formeditor.ftl.FtlComponentIdGenerator;
import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.bean.FtlComponent;
import ru.runa.gpd.formeditor.ftl.view.IToolPalleteDropEditor;
import ru.runa.gpd.formeditor.ftl.view.ToolPaletteMethodTag;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.formeditor.vartag.VarTagUtil;
import ru.runa.gpd.help.IHelpTopicProvider;
import ru.runa.gpd.htmleditor.editors.HTMLConfiguration;
import ru.runa.gpd.htmleditor.editors.HTMLSourceEditor;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ui.view.SelectionProvider;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.ValidationUtil;
import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * The WYSIWYG HTML editor using <a href="http://www.fckeditor.net/">FCKeditor</a>.
 * <p>
 * org.eclipse.ui.texteditor.BasicTextEditorActionContributor
 * </p>
 */
public class WYSIWYGHTMLEditor extends MultiPageEditorPart implements IResourceChangeListener, IToolPalleteDropEditor {
    private static final Pattern HTML_CONTENT_PATTERN = Pattern.compile("^(.*?<(body|BODY).*?>)(.*?)(</(body|BODY)>.*?)$", Pattern.DOTALL);

    public static final int CLOSED = 197;

    private HTMLSourceEditor sourceEditor;
    private Display display;
    private Browser browser;
    private ISelectionProvider selectionProvider;
    private IHelpTopicProvider helpTopicProvider;

    private boolean ftlFormat = true;
    private FormNode formNode;
    private IFile formFile;
    private final Map<Integer, FtlComponent> ftlComponents = Maps.newConcurrentMap();

    private boolean dirty = false;
    private boolean browserLoaded = false;
    private static WYSIWYGHTMLEditor lastInitializedInstance;

    public static final String ID = "ru.runa.gpd.wysiwyg.WYSIWYGHTMLEditor";

    private int cachedForVariablesCount = -1;
    private final Map<String, Map<String, Variable>> cachedVariables = new HashMap<String, Map<String, Variable>>();

    protected synchronized boolean isBrowserLoaded() {
        return browserLoaded;
    }

    protected synchronized void setBrowserLoaded(boolean browserLoaded) {
        this.browserLoaded = browserLoaded;
    }

    public boolean isFtlFormat() {
        return ftlFormat;
    }

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        formFile = ((FileEditorInput) editorInput).getFile();
        ftlFormat = editorInput.getName().endsWith("ftl");
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        lastInitializedInstance = this;
        IFile definitionFile = IOUtils.getProcessDefinitionFile((IFolder) formFile.getParent());
        ProcessDefinition processDefinition = ProcessCache.getProcessDefinition(definitionFile);
        if (formFile.getName().startsWith(ParContentProvider.SUBPROCESS_DEFINITION_PREFIX)) {
            String subprocessId = formFile.getName().substring(0, formFile.getName().indexOf("."));
            processDefinition = processDefinition.getEmbeddedSubprocessById(subprocessId);
            Preconditions.checkNotNull(processDefinition, "embedded subpocess");
        }
        for (FormNode formNode : processDefinition.getChildren(FormNode.class)) {
            if (editorInput.getName().equals(formNode.getFormFileName())) {
                this.formNode = formNode;
                setPartName(formNode.getName());
                break;
            }
        }
        if (formNode == null) {
            // user edit description
            return;
        }

        selectionProvider = new SelectionProvider();
        getSite().setSelectionProvider(selectionProvider);

        helpTopicProvider = new FtlComponentHelpProvider();

        addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(Object source, int propId) {
                if (propId == WYSIWYGHTMLEditor.CLOSED && formFile.exists()) {
                    createOrUpdateFormValidation();
                }
            }
        });
    }

    public Map<String, Variable> getVariables(boolean filterVariablesWithSpaces) {
        return getVariables(filterVariablesWithSpaces, null);
    }

    public synchronized Map<String, Variable> getVariables(boolean filterVariablesWithSpaces, String typeClassNameFilter) {
        if (formNode == null) {
            // This is because earlier access from web page (not user request)
            return new HashMap<String, Variable>();
        }
        List<Variable> variables = formNode.getProcessDefinition().getVariables(true, true);
        if (cachedForVariablesCount != variables.size()) {
            cachedForVariablesCount = variables.size();
            cachedVariables.clear();
        }
        if (!cachedVariables.containsKey(typeClassNameFilter)) {
            // get variables without strong-typing. (all hierarchy)
            if (typeClassNameFilter != null && !Object.class.getName().equals(typeClassNameFilter)) {
                List<String> filterHierarchy = VariableFormatRegistry.getInstance().getSuperClassNames(typeClassNameFilter);
                for (Variable variable : new ArrayList<Variable>(variables)) {
                    boolean applicable = false;
                    for (String className : filterHierarchy) {
                        if (VariableFormatRegistry.isAssignableFrom(variable.getJavaClassName(), typeClassNameFilter)
                                || VariableFormatRegistry.isAssignableFrom(typeClassNameFilter, variable.getJavaClassName())) {
                            if (VariableFormatRegistry.isApplicable(variable, className)) {
                                applicable = true;
                                break;
                            }
                        }
                    }
                    if (!applicable) {
                        variables.remove(variable);
                    }
                }
            }
            if (filterVariablesWithSpaces) {
                variables = VariableUtils.getValidVariables(variables);
            }
            cachedVariables.put(typeClassNameFilter, VariableUtils.toMap(variables));
        }
        return cachedVariables.get(typeClassNameFilter);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == ITextEditor.class) {
            return sourceEditor;
        } else if (adapter == IHelpTopicProvider.class) {
            return helpTopicProvider;
        }
        return super.getAdapter(adapter);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (sourceEditor != null && sourceEditor.getEditorInput() != null) {
            EditorUtils.closeEditorIfRequired(event, ((IFileEditorInput) sourceEditor.getEditorInput()).getFile(), this);
        }
    }

    @Override
    protected void createPages() {
        sourceEditor = new HTMLSourceEditor(new HTMLConfiguration(EditorsPlugin.getDefault().getColorProvider()));
        int pageNumber = 0;
        try {
            browser = new Browser(getContainer(), SWT.NULL);
            browser.addOpenWindowListener(new BrowserWindowHelper(getContainer().getDisplay()));
            new GetHTMLCallbackFunction(browser);
            new OnLoadCallbackFunction(browser);
            new MarkEditorDirtyCallbackFunction(browser);
            browser.addProgressListener(new ProgressAdapter() {
                @Override
                public void completed(ProgressEvent event) {
                    if (EditorsPlugin.DEBUG) {
                        PluginLogger.logInfo("completed " + event);
                    }
                }
            });
            addPage(browser);
            setPageText(pageNumber++, Messages.getString("wysiwyg.design.tab_name"));
        } catch (Throwable th) {
            PluginLogger.logError(Messages.getString("wysiwyg.design.create_error"), th);
        }
        try {
            addPage(sourceEditor, getEditorInput());
            setPageText(pageNumber++, Messages.getString("wysiwyg.source.tab_name"));
        } catch (Exception ex) {
            PluginLogger.logError(Messages.getString("wysiwyg.source.create_error"), ex);
        }
        if (browser == null) {
            return;
        }
        ConnectorServletHelper.setBaseDir(sourceEditor.getFile().getParent());
        ConnectorServletHelper.sync();
        try {
            final Display display = Display.getCurrent();
            final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(getSite().getShell());
            final IRunnableWithProgress runnable = new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask(Messages.getString("editor.task.init_wysiwyg"), 10);
                        WebServerUtils.startWebServer(monitor, 9);
                        monitor.subTask(Messages.getString("editor.subtask.waiting_init"));
                        display.asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                monitorDialog.setCancelable(true);
                                if (!browser.isDisposed()) {
                                    browser.setUrl(WebServerUtils.getEditorURL());
                                }
                            }
                        });
                        monitorDialog.setCancelable(true);
                        while (!isBrowserLoaded() && !monitor.isCanceled()) {
                            Thread.sleep(1000);
                        }
                        monitor.worked(1);
                        display.asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                if (!browser.isDisposed()) {
                                    setActivePage(0);
                                }
                            }
                        });
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            };
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        monitorDialog.run(true, false, runnable);
                    } catch (InvocationTargetException e) {
                        PluginLogger.logError(Messages.getString("wysiwyg.design.create_error"), e.getTargetException());
                    } catch (InterruptedException e) {
                        EditorsPlugin.logError("Web editor page", e);
                    }
                }
            });
        } catch (Exception e) {
            MessageDialog.openError(getContainer().getShell(), Messages.getString("wysiwyg.design.create_error"), e.getCause().getMessage());
            EditorsPlugin.logError("Web editor page", e);
        }
    }

    // Used from servlets
    public static WYSIWYGHTMLEditor getCurrent() {
        IEditorPart editor = EditorsPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getActivePage().getActiveEditor();
        if (editor instanceof WYSIWYGHTMLEditor) {
            return (WYSIWYGHTMLEditor) editor;
        }
        if (lastInitializedInstance != null) {
            return lastInitializedInstance;
        }
        throw new RuntimeException("No editor instance initialized");
    }

    private void setDirty(boolean dirty) {
        boolean changedDirtyState = this.dirty != dirty;
        this.dirty = dirty;
        if (changedDirtyState) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public boolean isDirty() {
        return dirty || sourceEditor.isDirty();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (getActivePage() == 0) {
            if (!syncBrowser2Editor()) {
                throw new InternalApplicationException(Messages.getString("wysiwyg.design.save_error"));
            }
        }
        sourceEditor.doSave(monitor);
        if (formNode != null) {
            formNode.setDirty();
            createOrUpdateFormValidation();
        }
        browser.execute("setHTMLSaved()");
        setDirty(false);
    }

    private void createOrUpdateFormValidation() {
        String op = "create";
        try {
            if (!formNode.hasFormValidation()) {
                String fileName = formNode.getId() + "." + FormNode.VALIDATION_SUFFIX;
                formNode.setValidationFileName(fileName);
                ValidationUtil.createNewValidationUsingForm(formFile, formNode);
            } else {
                op = "update";
                ValidationUtil.updateValidation(formFile, formNode);
            }
        } catch (Exception e) {
            PluginLogger.logError("Failed to " + op + " form validation", e);
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void dispose() {
        firePropertyChange(CLOSED);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    @Override
    protected void pageChange(int newPageIndex) {
        if (isBrowserLoaded()) {
            if (newPageIndex == 1) {
                syncBrowser2Editor();
            } else if (newPageIndex == 0) {
                ConnectorServletHelper.sync();
                syncEditor2Browser();
            }
        } else if (EditorsPlugin.DEBUG) {
            PluginLogger.logInfo("pageChange to = " + newPageIndex + " but editor is not loaded yet");
        }
        super.pageChange(newPageIndex);
    }

    private boolean syncBrowser2Editor() {
        if (browser != null) {
            boolean result = browser.execute("getHTML()");
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("syncBrowser2Editor: " + result);
            }
            return result;
        }
        return false;
    }

    private void syncEditor2Browser() {
        String html = getSourceDocumentHTML();
        Matcher matcher = HTML_CONTENT_PATTERN.matcher(html);
        if (matcher.find()) {
            html = matcher.group(3);
        }
        if (isFtlFormat()) {
            ftlComponents.clear();
            try {
                html = FreemarkerUtil.transformToHtml(this, html);
            } catch (Exception e) {
                EditorsPlugin.logError("ftl WYSIWYGHTMLEditor.syncEditor2Browser()", e);
            }
        } else {
            html = VarTagUtil.toHtml(html);
        }
        html = html.replaceAll("\r\n", "\n");
        html = html.replaceAll("\r", "\n");
        html = html.replaceAll("\n", "\\\\n");
        html = html.replaceAll("'", "\\\\'");
        if (browser != null) {
            boolean result = browser.execute("setHTML('" + html + "')");
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("syncEditor2Browser = " + result);
            }
        }
    }

    private String getSourceDocumentHTML() {
        return sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).get();
    }

    public int createComponent(String tagName) {
        FtlComponent ftlComponent = createComponentWithoutAddToList(tagName);
        int componentId = FtlComponentIdGenerator.generate();
        ftlComponents.put(componentId, ftlComponent);
        return componentId;
    }

    public FtlComponent createComponentWithoutAddToList(String tagId) {
        FtlComponent ftlComponent = new FtlComponent(MethodTag.getTagNotNull(tagId));
        ftlComponent.setProcessDefinition(formNode.getProcessDefinition());
        ftlComponent.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                WYSIWYGHTMLEditor editor = WYSIWYGHTMLEditor.this;
                if (editor != null && !editor.isDirty()) {
                    editor.setDirty(true);
                }
            }
        });
        return ftlComponent;
    }

    public FtlComponent getComponent(int componentId) {
        return ftlComponents.get(componentId);
    }

    public void removeComponent(int componentId) {
        ftlComponents.remove(componentId);
    }

    public void replaceComponent(int componentId, FtlComponent component) {
        ftlComponents.remove(componentId);
        ftlComponents.put(componentId, component);
    }

    public void componentSelected(int componentId) throws PartInitException, ExecutionException, NotDefinedException, NotEnabledException,
            NotHandledException {
        FtlComponent ftlComponent = ftlComponents.get(componentId);
        final ISelection selection = new StructuredSelection(ftlComponent);
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                selectionProvider.setSelection(selection);
            }
        });
    }

    public void openTagDialog(final int componentId) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                boolean changed = DialogCreatorHelper.openDialog(componentId);
                if (changed) {
                    try {
                        syncBrowser2Editor();
                        ConnectorServletHelper.sync();
                        syncEditor2Browser();
                    } catch (Exception e) {
                        PluginLogger.logError(e);
                    }
                }
            }
        });
    }

    public void componentDeselected() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                selectionProvider.setSelection(StructuredSelection.EMPTY);
            }
        });
    }

    public void openHelp(final String url) {
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url);
                } catch (Exception e) {
                    EditorsPlugin.logError("Can not display help", e);
                }
            }
        });
    }

    private class GetHTMLCallbackFunction extends BrowserFunction {
        public GetHTMLCallbackFunction(Browser browser) {
            super(browser, "getHTMLCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            String html = (String) arguments[0];
            if (isFtlFormat()) {
                try {
                    html = FreemarkerUtil.transformFromHtml(html, getVariables(false, null), ftlComponents);
                    Matcher matcher = HTML_CONTENT_PATTERN.matcher(html);
                    if (matcher.find()) {
                        html = matcher.group(3);
                    }
                    if (EditorsPlugin.DEBUG) {
                        PluginLogger.logInfo("Designer html = " + html);
                    }
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("freemarker html transform", e);
                    throw new InternalApplicationException(Messages.getString("wysiwyg.design.transform_to_source_error"));
                }
            } else {
                // bug in closing customtag tag
                html = VarTagUtil.fromHtml(html);
            }
            if (!html.equals(sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).get())) {
                sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).set(html);
            }
            return null;
        }
    }

    @Override
    public List<ToolPaletteMethodTag> getAllMethods() {
        List<ToolPaletteMethodTag> tags = new ArrayList<ToolPaletteMethodTag>();
        Map<String, Variable> variables = getVariables(false);

        for (MethodTag methodTag : MethodTag.getEnabled()) {
            try {
                InputStream imageStream = null;
                String tagImageName = null;
                if (MethodTag.hasTag(methodTag.id)) {
                    MethodTag tag = MethodTag.getTagNotNull(methodTag.id);
                    try {
                        if (tag.hasImage()) {
                            imageStream = tag.openImageStream();
                        }
                    } catch (IOException e) {
                        // Unable to load tag image, using default
                    }
                    tagImageName = "DefaultTag.png";
                } else {
                    if (variables.keySet().contains(methodTag.id)) {
                        tagImageName = "VariableValueDisplay.png";
                    } else {
                        tagImageName = "TagNotFound.png";
                    }
                }
                if (imageStream == null) {
                    imageStream = new ByteArrayInputStream(EditorsPlugin.loadTagImage(EditorsPlugin.getDefault().getBundle(), "metadata/icons/"
                            + tagImageName));
                }
                tags.add(new ToolPaletteMethodTag("${" + methodTag.id + "()}", methodTag.name, methodTag.helpPage, new Image(null, imageStream)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return tags;
    }

    public void doDrop() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                syncBrowser2Editor();
                ConnectorServletHelper.sync();
                syncEditor2Browser();
            }
        });
    }

    private class OnLoadCallbackFunction extends BrowserFunction {
        public OnLoadCallbackFunction(Browser browser) {
            super(browser, "onLoadCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            if (EditorsPlugin.DEBUG) {
                PluginLogger.logInfo("Invoked OnLoadCallbackFunction");
            }
            setBrowserLoaded(true);
            return null;
        }
    }

    private class MarkEditorDirtyCallbackFunction extends BrowserFunction {

        public MarkEditorDirtyCallbackFunction(Browser browser) {
            super(browser, "markEditorDirtyCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            setDirty(true);
            return null;
        }
    }

    @Override
    public void doDrop(String tagName) {
        syncBrowser2Editor();
        ConnectorServletHelper.sync();
        syncEditor2Browser();
    }

}
