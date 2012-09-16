package ru.runa.bpm.ui.sync;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.resource.Messages;

public abstract class DataImporter {

    private final IConnector connector;

    protected DataImporter(IConnector connector) {
        this.connector = connector;
    }

    public boolean isConfigured() {
        return connector.isConfigured();
    }

    public void connect() throws Exception {
        connector.connect();
    }

    protected File getCacheFile() {
        String fileName = getClass().getSimpleName() + ".xml";
        return new File(DesignerPlugin.getPreferencesFolder(), fileName);
    }

    protected abstract void clearInMemoryCache();

    protected abstract void loadRemoteData(IProgressMonitor monitor) throws Exception;

    protected abstract void saveCachedData() throws Exception;

    public abstract Object loadCachedData() throws Exception;

    public boolean hasCachedData() {
        try {
            return loadCachedData() != null;
        } catch (Exception e) {
            DesignerLogger.logErrorWithoutDialog("", e);
            return false;
        }
    }

    // TODO move to Connector
    // public final void connectWithRunnable() {
    // final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
    // monitorDialog.setCancelable(true);
    // final IRunnableWithProgress runnable = new IRunnableWithProgress() {
    //
    // public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    // try {
    // monitor.beginTask(Messages.getString("task.Connect"), 1);
    // connect();
    // } catch (Exception e) {
    // DesignerLogger.logErrorWithoutDialog("error.Connect", e);
    // throw new InvocationTargetException(e);
    // } finally {
    // monitor.done();
    // }
    // }
    // };
    // try {
    // monitorDialog.run(true, false, runnable);
    // } catch (InvocationTargetException ex) {
    // throw new RuntimeException(ex.getTargetException());
    // } catch (InterruptedException ex) {
    // //
    // }
    // }

    public final void synchronize() {
        Shell shell = Display.getCurrent() != null ? Display.getCurrent().getActiveShell() : null;
        final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(shell);
        monitorDialog.setCancelable(true);
        final IRunnableWithProgress runnable = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    monitor.beginTask(Messages.getString("task.SynchronizeData"), 120);
                    monitor.subTask(Messages.getString("task.Connect"));
                    connect();
                    monitor.worked(10);
                    monitor.subTask(Messages.getString("task.LoadData"));
                    clearInMemoryCache();
                    loadRemoteData(monitor);
                    // monitor.worked(1);
                    monitor.subTask(Messages.getString("task.SaveData"));
                    saveCachedData();
                    monitor.done();
                } catch (Exception e) {
                    DesignerLogger.logErrorWithoutDialog("error.Synchronize", e);
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            monitorDialog.run(true, false, runnable);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getTargetException());
        } catch (InterruptedException ex) {
            // 
        }
    }

}
