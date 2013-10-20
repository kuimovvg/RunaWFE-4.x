package ru.cg.runaex.components_plugin.property_editor.database;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.Shell;

import ru.runa.gpd.ui.view.DBResourcesContentProvider;

/**
 * @author Kochetkov
 */
public class TableContentProvider extends DBResourcesContentProvider {

    public TableContentProvider(Shell shell, IProject project) {
        super(shell, project);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement.getClass().getName().equals("org.apache.ddlutils.model.Table")) {
            return null;
        }
        return super.getChildren(parentElement);
    }

}