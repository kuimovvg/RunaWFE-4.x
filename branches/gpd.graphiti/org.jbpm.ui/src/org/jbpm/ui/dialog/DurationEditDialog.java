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
import org.jbpm.ui.common.model.ITimed;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.TimerDuration;
import org.jbpm.ui.util.TimerDuration.Unit;

public class DurationEditDialog extends Dialog {
    private static final int CLEAR_ID = 111;
    private final TimerDuration editable;
    private final ProcessDefinition definition;
    private Text baseDateField;
    private Text delayField;
    private Text unitField;
    
    public DurationEditDialog(ProcessDefinition definition, TimerDuration duration) {
        super(Display.getCurrent().getActiveShell());
        this.definition = definition;
        editable = new TimerDuration(duration != null ? duration.getDuration() : TimerDuration.EMPTY);
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
            label.setText(Messages.getString("property.duration.baseDate"));
        }
        {
            baseDateField = new Text(area, SWT.BORDER);
            baseDateField.setEditable(false);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            baseDateField.setLayoutData(gridData);

            Button button = new Button(area, SWT.PUSH);
            button.setText("...");
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ChooseDateVariableDialog dialog = new ChooseDateVariableDialog(definition, ITimed.CURRENT_DATE_MESSAGE);
                    editable.setVariableName(dialog.openDialog());
                    updateGUI();
                }
            });
        }

        {
            Label label = new Label(area, SWT.NO_BACKGROUND);
            GridData data = new GridData();
            data.horizontalSpan = 2;
            label.setLayoutData(data);
            label.setText(Messages.getString("property.duration.delay"));
        }
        {
            delayField = new Text(area, SWT.MULTI | SWT.BORDER);
            delayField.setEditable(false);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            gridData.minimumHeight = 200;
            delayField.setLayoutData(gridData);

            Button button = new Button(area, SWT.PUSH);
            button.setText("...");
            gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            button.setLayoutData(gridData);
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    NumberInputDialog inputDialog = new NumberInputDialog(editable.getDelay());
                    if (inputDialog.open() == IDialogConstants.OK_ID) {
                        editable.setDelay(inputDialog.getUserInput());
                        updateGUI();
                    }
                }
            });
        }

        {
            final Label label = new Label(area, SWT.NO_BACKGROUND);
            GridData data = new GridData();
            data.horizontalSpan = 2;
            label.setLayoutData(data);
            label.setText(Messages.getString("property.duration.format"));
            unitField = new Text(area, SWT.BORDER);
            unitField.setEditable(false);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            unitField.setLayoutData(gridData);

            Button button = new Button(area, SWT.PUSH);
            button.setText("...");
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ChooseItemDialog dialog = new ChooseItemDialog(label.getText(), "", false);
                    dialog.setItems(TimerDuration.getUnits());
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        editable.setUnit((Unit) dialog.getSelectedItem());
                        updateGUI();
                    }
                }
            });
        }
        return area;
    }

    private void updateGUI() {
        if (editable.getVariableName() != null) {
            baseDateField.setText(editable.getVariableName());
        } else {
            baseDateField.setText(ITimed.CURRENT_DATE_MESSAGE);
        }
        delayField.setText(editable.getDelay());
        unitField.setText(editable.getUnit().toString());
        boolean valid = false;
        try {
            new TimerDuration(editable.getDuration());
            valid = true;
        } catch (Throwable th) {
        }
        getButton(IDialogConstants.OK_ID).setEnabled(valid);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("property.duration"));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button button = createButton(parent, CLEAR_ID, Messages.getString("button.clear"), false);
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editable.setDuration(TimerDuration.EMPTY);
                updateGUI();
            }
        });
        updateGUI();
    }

    public Object openDialog() {
        if (open() == IDialogConstants.OK_ID) {
            return editable;
        }
        return null;
    }

}
