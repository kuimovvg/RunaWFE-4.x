package ru.runa.gpd.formeditor.wysiwyg;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
import org.eclipse.ui.internal.UIPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.formeditor.WYSIWYGPlugin;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil;
import ru.runa.gpd.formeditor.ftl.FtlComponentIdGenerator;
import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import ru.runa.gpd.formeditor.ftl.MethodTag.VariableAccess;
import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.bean.FtlComponent;
import ru.runa.gpd.formeditor.ftl.bean.ParameterFactory;
import ru.runa.gpd.formeditor.ftl.view.IToolPalleteDropEditor;
import ru.runa.gpd.formeditor.ftl.view.ToolPalleteMethodTag;
import ru.runa.gpd.formeditor.vartag.VarTagUtil;
import ru.runa.gpd.help.IHelpToticProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.view.IDBExplorerDropEditor;
import ru.runa.gpd.ui.view.SelectionProvider;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.ProjectFinder;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.util.VariableUtils;
import tk.eclipse.plugin.htmleditor.HTMLPlugin;
import tk.eclipse.plugin.htmleditor.editors.HTMLConfiguration;
import tk.eclipse.plugin.htmleditor.editors.HTMLSourceEditor;

/**
 * The WYSIWYG HTML editor using <a
 * href="http://www.fckeditor.net/">FCKeditor</a>.
 * <p>
 * org.eclipse.ui.texteditor.BasicTextEditorActionContributor
 * </p>
 */
public class WYSIWYGHTMLEditor extends MultiPageEditorPart implements IResourceChangeListener, IToolPalleteDropEditor, IDBExplorerDropEditor {
    private static final Pattern HTML_CONTENT_PATTERN = Pattern.compile("^(.*?<(body|BODY).*?>)(.*?)(</(body|BODY)>.*?)$", Pattern.DOTALL);

    public static final int CLOSED = 197;
    public static final String ID = "tk.eclipse.plugin.wysiwyg.WYSIWYGHTMLEditor";
    private HTMLSourceEditor sourceEditor;
    private Display display;
    private Browser browser;
    private ISelectionProvider selectionProvider;
    private IHelpToticProvider helpTopicProvider;

    private boolean ftlFormat = true;
    private FormNode formNode;
    private Map<Integer, FtlComponent> ftlComponents = new ConcurrentHashMap<Integer, FtlComponent>();

    private boolean dirty = false;
    private boolean browserLoaded = false;
    private Timer updateSaveButtonTimer;
    private String savedHTML = "";
    private Object editorLock = new Object();
    private static WYSIWYGHTMLEditor lastInitializedInstance;
    private Date tabSwitchTime;
    private static final boolean DEBUG = "true".equals(System.getProperty("ru.runa.gpd.form.ftl.debug"));

    private int cachedForVariablesCount = -1;
    private Map<String, Map<String, Variable>> cachedVariables = new HashMap<String, Map<String, Variable>>();

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
        final IFile formFile = ((FileEditorInput) editorInput).getFile();
        ftlFormat = editorInput.getName().endsWith("ftl");
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        lastInitializedInstance = this;
        
        IFile definitionFile = ProjectFinder.getProcessDefinitionFile((IFolder) formFile.getParent());
        ProcessDefinition processDefinition = ProcessCache.getProcessDefinition(definitionFile);
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
                    String op = "create";
                    try {
                        if (!formNode.hasFormValidation()) {
                            String validationFileName = formNode.getId() + "." + FormNode.VALIDATION_SUFFIX;
                            IFile validationFile = ValidationUtil.createNewValidationUsingForm(formFile, validationFileName, formNode);
                            formNode.setValidationFileName(validationFile.getName());
                        } else {
                            op = "update";
                            ValidationUtil.updateValidation(formFile, formNode);
                        }
                    } catch (Exception e) {
                        PluginLogger.logError("Failed to " + op + " form validation", e);
                    }
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
        List<Variable> variables = formNode.getProcessDefinition().getVariables(true);
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
                        if (VariableFormatRegistry.isApplicable(variable, className)) {
                            applicable = true;
                            break;
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
        } else if (adapter == IHelpToticProvider.class) {
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
        sourceEditor = new HTMLSourceEditor(new HTMLConfiguration(HTMLPlugin.getDefault().getColorProvider()));
        int pageNumber = 0;
        try {
            browser = new Browser(getContainer(), SWT.MOZILLA);
            browser.addOpenWindowListener(new BrowserWindowHelper(getContainer().getDisplay()));
            new GetHTMLCallbackFunction(browser);
            browser.addProgressListener(new ProgressAdapter() {
                @Override
                public void completed(ProgressEvent event) {
                    if (DEBUG) {
                        PluginLogger.logInfo("completed " + event);
                    }
                    if (updateSaveButtonTimer == null) {
                        updateSaveButtonTimer = new Timer("updateSaveButtonTimer");
                        updateSaveButtonTimer.schedule(new UpdateSaveButtonTimerTask(), 1000, 1000);
                        if (DEBUG) {
                            PluginLogger.logInfo("Started updateSaveButtonTimer");
                        }
                    } else if (DEBUG) {
                        PluginLogger.logInfo("tried to start timer more than once!");
                    }
                }
            });
            addPage(browser);
            setPageText(pageNumber++, WYSIWYGPlugin.getResourceString("wysiwyg.design.tab_name"));
        } catch (Throwable th) {
            PluginLogger.logError(WYSIWYGPlugin.getResourceString("wysiwyg.design.create_error"), th);
        }
        try {
            addPage(sourceEditor, getEditorInput());
            setPageText(pageNumber++, WYSIWYGPlugin.getResourceString("wysiwyg.source.tab_name"));
        } catch (Exception ex) {
            PluginLogger.logError(WYSIWYGPlugin.getResourceString("wysiwyg.source.create_error"), ex);
        }
        if (browser == null) {
            return;
        }
        ConnectorServletHelper.setBaseDir(sourceEditor.getFile().getParent());
        try {
            display = Display.getCurrent();
            final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(getSite().getShell());
            final IRunnableWithProgress runnable = new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask(WYSIWYGPlugin.getResourceString("editor.task.init_wysiwyg"), 10);
                        WYSIWYGPlugin.getDefault().startWebServer(monitor, 9);
                        monitor.subTask(WYSIWYGPlugin.getResourceString("editor.subtask.waiting_init"));
                        display.asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                monitorDialog.setCancelable(true);
                                browser.setUrl(WYSIWYGPlugin.getDefault().getEditorURL());
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
                                setActivePage(0);
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
                        PluginLogger.logError(WYSIWYGPlugin.getResourceString("wysiwyg.design.create_error"), e.getTargetException());
                    } catch (InterruptedException e) {
                        WYSIWYGPlugin.logError("Web editor page", e);
                    }
                }
            });
            savedHTML = getSourceDocumentHTML();
        } catch (Exception e) {
            MessageDialog.openError(getContainer().getShell(), WYSIWYGPlugin.getResourceString("wysiwyg.design.create_error"), e.getCause().getMessage());
            WYSIWYGPlugin.logError("Web editor page", e);
        }
    }

    private IFile getFormFile() {
        IFileEditorInput input = (IFileEditorInput) getEditorInput();
        IFile formFile = input.getFile();
        return formFile;
    }

    // Used from servlets
    public static WYSIWYGHTMLEditor getCurrent() {
        IEditorPart editor = UIPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getActivePage().getActiveEditor();
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
    public void doDrop(String tag) {
        syncBrowser2Editor();
        ConnectorServletHelper.sync();
        syncEditor2Browser();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (getActivePage() == 0) {
            syncBrowser2Editor();
        }
        sourceEditor.doSave(monitor);
        savedHTML = getSourceDocumentHTML();
        if (formNode != null) {
            formNode.validateFormContent(getFormFile());
            formNode.setDirty();
        }
        setDirty(false);
    }

    @Override
    public boolean isSaveOnCloseNeeded() {
        if (getActivePage() == 0) {
            syncBrowser2Editor();
            return true;
        }
        return super.isSaveOnCloseNeeded();
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
        if (updateSaveButtonTimer != null) {
            updateSaveButtonTimer.cancel();
        }
        firePropertyChange(CLOSED);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    @Override
    protected void pageChange(int newPageIndex) {
        synchronized (editorLock) {
            if (newPageIndex == 1) {
                syncBrowser2Editor();
            } else if (newPageIndex == 0) {
                ConnectorServletHelper.sync();
                syncEditor2Browser();
            }
            super.pageChange(newPageIndex);
            tabSwitchTime = new Date();
        }
    }

    private void syncBrowser2Editor() {
        if (browser != null) {
            boolean result = browser.execute("getHTML(false, " + System.currentTimeMillis() + ")");
            if (DEBUG) {
                PluginLogger.logInfo("syncBrowser2Editor: " + result);
            }
        }
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
                WYSIWYGPlugin.logError("ftl WYSIWYGHTMLEditor.syncEditor2Browser()", e);
            }
        }
        html = html.replaceAll("\r\n", "\n");
        html = html.replaceAll("\r", "\n");
        html = html.replaceAll("\n", "\\\\n");
        html = html.replaceAll("'", "\\\\'");
        if (browser != null) {
            boolean result = browser.execute("setHTML('" + html + "')");
            if (DEBUG) {
                PluginLogger.logInfo("syncEditor2Browser = " + result);
            }
        }
    }

    private String getDesignDocumentHTML(String html) {
        if (isFtlFormat()) {
            try {
                html = FreemarkerUtil.transformFromHtml(html, getVariables(false), ftlComponents);
                Matcher matcher = HTML_CONTENT_PATTERN.matcher(html);
                if (matcher.find()) {
                    html = matcher.group(3);
                }
                if (DEBUG) {
                    PluginLogger.logInfo("Designer html = " + html);
                }
                return html;
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("freemarker html transform", e);
                return null;
            }
        } else {
            // bug in closing customtag tag
            return VarTagUtil.normalizeVarTags(html);
        }
    }

    private String getSourceDocumentHTML() {
        return sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).get();
    }

    public int createComponent(String tagName) {
        int componentId = FtlComponentIdGenerator.generate();
        FtlComponent ftlComponent = new FtlComponent(MethodTag.getTagNotNull(tagName));
        ftlComponent.setProcessDefinition(formNode.getProcessDefinition());
        ftlComponents.put(componentId, ftlComponent);
        return componentId;
    }

    public FtlComponent getComponent(int componentId) {
        return ftlComponents.get(componentId);
    }

    public void componentSelected(int componentId) throws PartInitException, ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
        final ISelection selection = new StructuredSelection(ftlComponents.get(componentId));
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                selectionProvider.setSelection(selection);
            }
        });
    }

    public void componentDeselected() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                selectionProvider.setSelection(StructuredSelection.EMPTY);
            }
        });
    }

    public void openHelp(final String url) {
        display.asyncExec(new Runnable() {
            public void run() {
                try {
                    PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url);
                } catch (Exception e) {
                    WYSIWYGPlugin.logError("Can not display help", e);
                }
            }
        });
    }

    private class UpdateSaveButtonTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (getActivePage() == 0 && browser != null) {
                    synchronized (editorLock) {
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    browser.execute("getHTML(true, " + System.currentTimeMillis() + ")");
                                } catch (Throwable e) {
                                    if (DEBUG) {
                                        PluginLogger.logInfo(e.getMessage());
                                    }
                                }
                            }
                        });
                    }
                }
            } catch (Throwable e) {
                if (DEBUG) {
                    PluginLogger.logInfo(e.getMessage());
                }
            }
        }
    }

    private class GetHTMLCallbackFunction extends BrowserFunction {
        public GetHTMLCallbackFunction(Browser browser) {
            super(browser, "getHTMLCallback");
        }

        @Override
        public Object function(Object[] arguments) {
            boolean sync = (Boolean) arguments[0];
            Long eventTime = ((Double) arguments[1]).longValue();
            String text = (String) arguments[2];
            if (eventTime <= tabSwitchTime.getTime())
                return null;

            String html = !text.isEmpty() ? getDesignDocumentHTML(text) : "";
            if (html == null) {
                return null;
            }

            if (sync) {
                String oldContent = getSourceDocumentHTML();
                if (!oldContent.equals(html)) {
                    sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).set(html);
                    setDirty(true);
                } else if (DEBUG) {
                    PluginLogger.logInfo("Nothing to change: " + html);
                }
            } else {
                String diff = StringUtils.difference(savedHTML, html);
                boolean setDirty = (diff.length() != 0);
                if (setDirty != isDirty()) {
                    setDirty(setDirty);
                } else if (DEBUG) {
                    PluginLogger.logInfo("Dirty state did not changed: " + setDirty);
                }
                if (setDirty) {
                    sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).set(html);
                }
            }
            return null;
        }
    }

    @Override
    public List<ToolPalleteMethodTag> getAllMethods() {
        List<ToolPalleteMethodTag> tags = new ArrayList<ToolPalleteMethodTag>();
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
                    imageStream = WYSIWYGPlugin.loadTagImage(WYSIWYGPlugin.getDefault().getBundle(), "metadata/icons/" + tagImageName);
                }
                tags.add(new ToolPalleteMethodTag("${" + methodTag.id + "()}", methodTag.name, methodTag.helpPage, new Image(null, imageStream)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return tags;
    }

    @Override
    public void doDrop() {
        tabSwitchTime = new Date();
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                syncBrowser2Editor();
                ConnectorServletHelper.sync();
                syncEditor2Browser();
            }
        });

    }
}
