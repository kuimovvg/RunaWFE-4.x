package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.MultiinstanceParameters;

public class MultiTaskExecutorsDialog extends Dialog {
    private final ProcessDefinition processDefinition;
    private final MultiinstanceParameters parameters;

    public MultiTaskExecutorsDialog(MultiTaskState state) {
        super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        this.processDefinition = state.getProcessDefinition();
        this.parameters = state.getMultiinstanceParameters();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setSize(600, 300);
        super.configureShell(newShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        Group group = new Group(composite, SWT.NONE);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setText(Localization.getString("Feature.Multiinstance"));
        MultiinstanceComposite multiinstanceComposite = new MultiinstanceComposite(group, processDefinition, parameters, 
                Localization.getString("Multiinstance.TasksVariableName"), Localization.getString("Multiinstance.TasksGroupName"));
        multiinstanceComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        multiinstanceComposite.addCreateVariableLink(Localization.getString("label.action.createExecutorsVariable"), 
                "ru.runa.wfe.var.format.ListFormat(ru.runa.wfe.var.format.ExecutorFormat)");
        return composite;
    }

    public String getExecutorsDiscriminatorUsage() {
        return parameters.getDiscriminatorMapping().getUsage();
    }

    public String getExecutorsDiscriminatorValue() {
        return parameters.getDiscriminatorMapping().getName();
    }

}
