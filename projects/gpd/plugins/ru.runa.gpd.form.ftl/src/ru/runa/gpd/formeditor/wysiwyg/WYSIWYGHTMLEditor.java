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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.UIPlugin;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.formeditor.WYSIWYGPlugin;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil;
import ru.runa.gpd.formeditor.vartag.VarTagUtil;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.EditorUtils;
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
public class WYSIWYGHTMLEditor extends MultiPageEditorPart implements IResourceChangeListener {
    public static final int CLOSED = 197;
    public static final String ID = "tk.eclipse.plugin.wysiwyg.WYSIWYGHTMLEditor";
    private HTMLSourceEditor sourceEditor;
    private Browser browser;
    private boolean ftlFormat = true;
    private FormNode formNode;
    private boolean dirty = false;
    private boolean browserLoaded = false;
    private static final Pattern pattern = Pattern.compile("^(.*?<(body|BODY).*?>)(.*?)(</(body|BODY)>.*?)$", Pattern.DOTALL);
    private Timer updateSaveButtonTimer;
    private String savedHTML = "";
    private static WYSIWYGHTMLEditor lastInitializedInstance;
    private static final boolean DEBUG = "true".equals(System.getProperty("ru.runa.gpd.form.ftl.debug"));

    protected synchronized boolean isBrowserLoaded() {
        return browserLoaded;
    }

    protected synchronized void setBrowserLoaded(boolean browserLoaded) {
        this.browserLoaded = browserLoaded;
    }

    public boolean isFtlFormat() {
        return ftlFormat;
    }

    public void setFormNode(FormNode formNode) {
        this.formNode = formNode;
    }

    public Map<String, Variable> getVariables(String typeClassNameFilter) {
        if (formNode == null) {
            // This is because earlier access from web page (not user request)
            return new HashMap<String, Variable>();
        }
        List<Variable> variables = formNode.getProcessDefinition().getVariables(true);
        // get variables without strong-typing. (all hierarchy) TODO for similar places
        if (typeClassNameFilter != null && !Object.class.getName().equals(typeClassNameFilter)) {
            List<String> filterHierarchy = VariableFormatRegistry.getInstance().getSuperClassNames(typeClassNameFilter);
            List<Variable> copyList = new ArrayList<Variable>(variables);
            for (Variable variable : copyList) {
                if (!filterHierarchy.contains(variable.getJavaClassName())) {
                    variables.remove(variable);
                }
            }
        }
        return VariableUtils.toMap(VariableUtils.getValidVariables(variables));
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
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        setPartName(editorInput.getName());
        ftlFormat = editorInput.getName().endsWith("ftl");
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        lastInitializedInstance = this;
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
            final Display display = Display.getCurrent();
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
            if (DEBUG) {
                PluginLogger.logInfo("syncBrowser2Editor: " + result);
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
                html = FreemarkerUtil.transformToHtml(getVariables(null), html);
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
                html = FreemarkerUtil.transformFromHtml(html, getVariables(null));
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    html = matcher.group(3);
                }
                if (DEBUG) {
                    PluginLogger.logInfo("Designer html = " + html);
                }
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("freemarker html transform", e);
            }
        } else {
            // bug in closing customtag tag
            html = VarTagUtil.normalizeVarTags(html);
        }
        return html;
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
                                if (DEBUG) {
                                    PluginLogger.logInfo(e.getMessage());
                                }
                            }
                        }
                    });
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
            String text = (String) arguments[1];
            if (text.length() == 0) {
                if (DEBUG) {
                    PluginLogger.logInfo("empty text received in callback for sync=" + sync);
                }
                return null;
            }
            if (sync) {
                String html = getDesignDocumentHTML(text);
                String oldContent = getSourceDocumentHTML();
                if (!oldContent.equals(html)) {
                    sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).set(html);
                    setDirty(true);
                } else if (DEBUG) {
                    PluginLogger.logInfo("Nothing to change: " + html);
                }
            } else {
                String html = getDesignDocumentHTML(text);
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
}
