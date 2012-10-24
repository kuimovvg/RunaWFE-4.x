package org.jbpm.ui.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.common.model.Delegable;
import org.jbpm.ui.custom.CustomizationRegistry;
import org.jbpm.ui.custom.DelegableProvider;

public class DelegableConfPropertyDescriptor extends PropertyDescriptor {
    private final Delegable delegable;

    public DelegableConfPropertyDescriptor(Object id, Delegable delegable, String displayName) {
        super(id, displayName);
        this.delegable = delegable;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new DelegableCellEditor(parent);
    }

    private class DelegableCellEditor extends DialogCellEditor {

        public DelegableCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            try {
                DelegableProvider provider = CustomizationRegistry.getProvider(delegable.getDelegationClassName());
                return provider.showConfigurationDialog(delegable);
            } catch (Exception e) {
                DesignerLogger.logError("Unable to open configuration dialog for " + delegable.getDelegationClassName(), e);
                return null;
            }
        }

        @Override
        protected void updateContents(Object value) {
            super.updateContents(value);
            getDefaultLabel().setToolTipText((String) value);
        }
        
    }

}
