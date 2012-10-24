package org.jbpm.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.TimerAction;
import org.jbpm.ui.custom.CustomizationRegistry;
import org.jbpm.ui.custom.DelegableProvider;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.TimerDuration;

public class EscalationActionEditDialog extends Dialog {
    private static final int DELETE_ID = 111;
    private final TimerAction editable;
    private final ProcessDefinition definition;
    private Button editConfigButton;
    private Text classNameField;
    private Text configField;
    private Text repeatField;
    //private final boolean deleteEnabled;
    
    public EscalationActionEditDialog(ProcessDefinition definition, TimerAction timerAction) {
        super(Display.getCurrent().getActiveShell());
        this.definition = definition;
        editable = new TimerAction(definition);
        //deleteEnabled = timerAction != null;
        if (timerAction != null) {
            editable.setDelegationClassName(timerAction.getDelegationClassName());
            editable.setDelegationConfiguration(timerAction.getDelegationConfiguration());
            editable.setRepeat(timerAction.getRepeat().getDuration());
        }
    }

    @Override
    public Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout(2, false));
        {
            Label label = new Label(area, SWT.NO_BACKGROUND);
            GridData gridData = new GridData();
            gridData.horizontalSpan = 2;
            label.setLayoutData(gridData);
            label.setText(Messages.getString("property.delegation.class"));
        }
        {
            classNameField = new Text(area, SWT.BORDER);
            classNameField.setEditable(false);
            classNameField.setEnabled(false);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            classNameField.setLayoutData(gridData);

            /*Button button = new Button(area, SWT.PUSH);
            button.setText("...");
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ChooseHandlerClassDialog dialog = new ChooseHandlerClassDialog(Delegable.ACTION_HANDLER);
                    String className = dialog.openDialog();
                    if (className != null) {
                        editable.setDelegationClassName(className);
                        updateGUI();
                    }
                }
            });*/
        }

        {
            Label label = new Label(area, SWT.NO_BACKGROUND);
            GridData data = new GridData();
            data.horizontalSpan = 2;
            label.setLayoutData(data);
            label.setText(Messages.getString("property.escalation.configuration"));
        }
        {
            configField = new Text(area, SWT.MULTI | SWT.BORDER);
            configField.setEditable(false);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.widthHint = 300;
            gridData.heightHint = 100;
            configField.setLayoutData(gridData);

            Button button = new Button(area, SWT.PUSH);
            button.setText("...");
            gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            button.setLayoutData(gridData);
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    try {
                        DelegableProvider provider = CustomizationRegistry.getProvider(editable.getDelegationClassName());
                        String config = provider.showConfigurationDialog(editable);
                        if (config != null) {
                            editable.setDelegationConfiguration(config);
                        }
                    } catch (Exception ex) {
                        DesignerLogger.logError("Unable to open configuration dialog for " + editable.getDelegationClassName(), ex);
                    }
                    updateGUI();
                }
            });
            editConfigButton = button;
        }

        {
            Label label = new Label(area, SWT.NO_BACKGROUND);
            GridData data = new GridData();
            data.horizontalSpan = 2;
            label.setLayoutData(data);
            label.setText(Messages.getString("property.timer.repeat"));
        }
        {
            repeatField = new Text(area, SWT.BORDER);
            repeatField.setEditable(false);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            repeatField.setLayoutData(gridData);

            Button button = new Button(area, SWT.PUSH);
            button.setText("...");
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    DurationEditDialog dialog = new DurationEditDialog(definition, editable.getRepeat());
                    TimerDuration duration = (TimerDuration) dialog.openDialog();
                    if (duration != null) {
                        editable.setRepeat(duration.getDuration());
                        updateGUI();
                    }
                }
            });
        }
        return area;
    }

    private void updateGUI() {
        classNameField.setText(editable.getDelegationClassName());
        configField.setText(editable.getDelegationConfiguration());
        if (editable.getRepeat().hasDuration()) {
            repeatField.setText(editable.getRepeat().toString());
        } else {
            repeatField.setText(Messages.getString("duration.norepeat"));
        }
        editConfigButton.setEnabled(editable.isValid());
        getButton(IDialogConstants.OK_ID).setEnabled(editable.isValid());
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("escalation.action"));
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        //Button button = createButton(parent, DELETE_ID, Messages.getString("button.delete"), false);
        //button.setEnabled(deleteEnabled);
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        /*button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnCode(DELETE_ID);
                close();
            }
        });*/
        updateGUI();
    }

    public Object openDialog() {
        int buttonId = open();
        if (buttonId == DELETE_ID) {
            return TimerAction.NONE;
        }
        if (buttonId == IDialogConstants.OK_ID) {
            return editable;
        }
        return null;
    }

}
