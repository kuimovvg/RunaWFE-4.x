/*
 * Created on 24.09.2005
 */
package ru.runa.gpd.ui.view;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ddlutils.model.Schema;
import org.apache.ddlutils.model.Table;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.util.DatabaseStructureUtil;

/**
 * @author Kochetkov
 */
public class DBResourcesContentProvider implements ITreeContentProvider {

    private List<Schema> dbExplorerResources;
    private Shell shell;
    private IProject project;

    public DBResourcesContentProvider(Shell shell, IProject project) {
        this.shell = shell;
        this.project = project;
        this.dbExplorerResources = new ArrayList<Schema>();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Schema) {
            return ((Schema) parentElement).getTablesAsArray();
        }
        if (parentElement instanceof Table) {
            return ((Table) parentElement).getColumns();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof Schema) {
            return ((Schema) element).getTableCount() > 0;
        }
        if (element instanceof Table) {
            return ((Table) element).getColumnCount() > 0;
        }
        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        try {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
            dialog.run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        List<Schema> dbExplorerResourcesForProject = DatabaseStructureUtil.getDbExplorerResourcesForProject(project, monitor);
                        if (dbExplorerResourcesForProject != null) {
                            dbExplorerResources.clear();
                            dbExplorerResources.addAll(dbExplorerResourcesForProject);
                        }
                    } catch (Exception ex) {
                        throw new InvocationTargetException(ex);
                    }
                }
            });

            return dbExplorerResources.toArray();
        } catch (Exception e) {
            PluginLogger.logError(Localization.getString("DBResourcesContentProvider.dbModelReadException"), e);
            return new Object[] {};
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public IProject getProject() {
        return this.project;
    }
}
