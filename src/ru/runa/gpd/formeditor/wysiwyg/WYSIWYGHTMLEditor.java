package ru.runa.gpd.formeditor.wysiwyg;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.UIPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.formeditor.WYSIWYGPlugin;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.formeditor.vartag.VarTagUtil;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.util.VariableUtils;
import tk.eclipse.plugin.htmleditor.HTMLPlugin;
import tk.eclipse.plugin.htmleditor.editors.HTMLConfiguration;
import tk.eclipse.plugin.htmleditor.editors.HTMLSourceEditor;

import com.google.common.base.Preconditions;

/**
 * The WYSIWYG HTML editor using <a
 * href="http://www.fckeditor.net/">FCKeditor</a>.
 * <p>
 * org.eclipse.ui.texteditor.BasicTextEditorActionContributor
 * </p>
 */
public class WYSIWYGHTMLEditor extends MultiPageEditorPart implements IResourceChangeListener {
    public static final int CLOSED = 197;
    public static final String ID = "tk.eclipse.plugin.wysiwyg.WYSIWYGHTMLEditor";
    private HTMLSourceEditor sourceEditor;
    private Browser browser;
    private boolean ftlFormat = true;
    private FormNode formNode;
    private IFile formFile;
    private boolean dirty = false;
    private boolean browserLoaded = false;
    private static final Pattern pattern = Pattern.compile("^(.*?<(body|BODY).*?>)(.*?)(</(body|BODY)>.*?)$", Pattern.DOTALL);
    private Timer updateSaveButtonTimer;
    private String savedHTML = "";
    private static WYSIWYGHTMLEditor lastInitializedInstance;

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
        addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(Object source, int propId) {
                if (propId == WYSIWYGHTMLEditor.CLOSED && formFile.exists()) {
                    createOrUpdateFormValidation();
                }
            }
        });
    }

    private int cachedForVariablesCount = -1;
    private Map<String, Map<String, Variable>> cachedVariables = new HashMap<String, Map<String, Variable>>();

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
            browser = new Browser(getContainer(), SWT.NULL);
            browser.addOpenWindowListener(new BrowserWindowHelper(getContainer().getDisplay()));
            new GetHTMLCallbackFunction(browser);
            browser.addProgressListener(new ProgressAdapter() {
                @Override
                public void completed(ProgressEvent event) {
                    if (WYSIWYGPlugin.DEBUG) {
                        PluginLogger.logInfo("completed " + event);
                    }
                    if (updateSaveButtonTimer == null) {
                        updateSaveButtonTimer = new Timer("updateSaveButtonTimer");
                        updateSaveButtonTimer.schedule(new UpdateSaveButtonTimerTask(), 1000, 1000);
                        if (WYSIWYGPlugin.DEBUG) {
                            PluginLogger.logInfo("Started updateSaveButtonTimer");
                        }
                    } else if (WYSIWYGPlugin.DEBUG) {
                        PluginLogger.logInfo("tried to start timer more than once!");
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
                                browser.setUrl(WebServerUtils.getEditorURL());
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
                        PluginLogger.logError(Messages.getString("wysiwyg.design.create_error"), e.getTargetException());
                    } catch (InterruptedException e) {
                        WYSIWYGPlugin.logError("Web editor page", e);
                    }
                }
            });
            savedHTML = getSourceDocumentHTML();
        } catch (Exception e) {
            MessageDialog.openError(getContainer().getShell(), Messages.getString("wysiwyg.design.create_error"), e.getCause().getMessage());
            WYSIWYGPlugin.logError("Web editor page", e);
        }
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
    public void doSave(IProgressMonitor monitor) {
        if (getActivePage() == 0) {
            syncBrowser2Editor();
        }
        sourceEditor.doSave(monitor);
        savedHTML = getSourceDocumentHTML();
        if (formNode != null) {
            formNode.setDirty();
            createOrUpdateFormValidation();
        }
        setDirty(false);
    }
    
    private void createOrUpdateFormValidation() {
        String op = "create";
        try {
            if (!formNode.hasFormValidation()) {
                String fileName = formNode.getId() + "." + FormNode.VALIDATION_SUFFIX;
                IFile validationFile = ValidationUtil.createNewValidationUsingForm(formFile, fileName, formNode);
                formNode.setValidationFileName(validationFile.getName());
            } else {
                op = "update";
                ValidationUtil.updateValidation(formFile, formNode);
            }
        } catch (Exception e) {
            PluginLogger.logError("Failed to " + op + " form validation", e);
        }
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
        if (newPageIndex == 1) {
            syncBrowser2Editor();
        } else if (newPageIndex == 0) {
            ConnectorServletHelper.sync();
            syncEditor2Browser();
        }
        super.pageChange(newPageIndex);
    }

    private void syncBrowser2Editor() {
        if (browser != null) {
            boolean result = browser.execute("getHTML(false)");
            if (WYSIWYGPlugin.DEBUG) {
                PluginLogger.logInfo("syncBrowser2Editor = " + result);
            }
        }
    }

    private void syncEditor2Browser() {
        String html = getSourceDocumentHTML();
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            html = matcher.group(3);
        }
        if (isFtlFormat()) {
            try {
                html = FreemarkerUtil.transformToHtml(getVariables(false, null), html);
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
            if (WYSIWYGPlugin.DEBUG) {
                PluginLogger.logInfo("syncEditor2Browser = " + result);
            }
        }
    }

    private String getDesignDocumentHTML(String html) {
        if (isFtlFormat()) {
            try {
                html = FreemarkerUtil.transformFromHtml(html, getVariables(false, null));
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    html = matcher.group(3);
                }
                if (WYSIWYGPlugin.DEBUG) {
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

    private class UpdateSaveButtonTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (getActivePage() == 0 && browser != null) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                browser.execute("getHTML(true)");
                            } catch (Throwable e) {
                                if (WYSIWYGPlugin.DEBUG) {
                                    PluginLogger.logInfo(e.getMessage());
                                }
                            }
                        }
                    });
                }
            } catch (Throwable e) {
                if (WYSIWYGPlugin.DEBUG) {
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
            String text = (String) arguments[1];
            if (text.length() == 0) {
                if (WYSIWYGPlugin.DEBUG) {
                    PluginLogger.logInfo("empty text received in callback for sync=" + sync);
                }
                return null;
            }
            if (sync) {
                String html = getDesignDocumentHTML(text);
                if (html == null) {
                    return null;
                }
                String oldContent = getSourceDocumentHTML();
                if (!oldContent.equals(html)) {
                    sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).set(html);
                    setDirty(true);
                } else if (WYSIWYGPlugin.DEBUG) {
                    PluginLogger.logInfo("Nothing to change: " + html);
                }
            } else {
                String html = getDesignDocumentHTML(text);
                if (html == null) {
                    return null;
                }
                String diff = StringUtils.difference(savedHTML, html);
                boolean setDirty = (diff.length() != 0);
                if (setDirty != isDirty()) {
                    setDirty(setDirty);
                } else if (WYSIWYGPlugin.DEBUG) {
                    PluginLogger.logInfo("Dirty state did not changed: " + setDirty);
                }
                if (setDirty) {
                    sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).set(html);
                }
            }
            return null;
        }
    }
}
