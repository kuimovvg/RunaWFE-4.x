package ru.runa.bpm.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InfoWithDetailsDialog extends IconAndMessageDialog {
    private Button detailsButton;
    private String title;
    private String detailsMessage;
    private Text text;

    public InfoWithDetailsDialog(String dialogTitle, String infoMessage, String detailsMessage) {
        super(Display.getCurrent().getActiveShell());
        this.title = dialogTitle;
        this.message = infoMessage;
        this.detailsMessage = detailsMessage;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void buttonPressed(int id) {
        if (id == IDialogConstants.DETAILS_ID) {
            // was the details button pressed?
            toggleDetailsArea();
        } else {
            super.buttonPressed(id);
        }
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
        createDetailsButton(parent);
    }

    protected void createDetailsButton(Composite parent) {
        detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        createMessageArea(parent);
        // create a composite with standard margins and spacing
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.numColumns = 2;
        composite.setLayout(layout);
        GridData childData = new GridData(GridData.FILL_BOTH);
        childData.horizontalSpan = 2;
        composite.setLayoutData(childData);
        composite.setFont(parent.getFont());
        return composite;
    }

    @Override
    protected void createDialogAndButtonArea(Composite parent) {
        super.createDialogAndButtonArea(parent);
        if (this.dialogArea instanceof Composite) {
            // Create a label if there are no children to force a smaller layout
            Composite dialogComposite = (Composite) dialogArea;
            if (dialogComposite.getChildren().length == 0) {
                new Label(dialogComposite, SWT.NULL);
            }
        }
    }

    @Override
    protected Image getImage() {
        return getQuestionImage();
    }

    protected void createDropDownList(Composite parent) {
        text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        text.setText(detailsMessage);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        text.setLayoutData(data);
    }

    public static int open(String title, String infoMessage, String detailsMessage) {
        InfoWithDetailsDialog dialog = new InfoWithDetailsDialog(title, infoMessage, detailsMessage);
        return dialog.open();
    }

    private void toggleDetailsArea() {
        Point windowSize = getShell().getSize();
        Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (isDetailsAreaCreated()) {
            text.dispose();
            detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
        } else {
            createDropDownList((Composite) getContents());
            detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
        }
        Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
    }
    
    private boolean isDetailsAreaCreated() {
        return (text != null && !text.isDisposed());
    }

    protected final void showDetailsArea() {
        if (!isDetailsAreaCreated()) {
            Control control = getContents();
            if (control != null && !control.isDisposed()) {
                toggleDetailsArea();
            }
        }
    }

}
