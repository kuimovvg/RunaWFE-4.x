package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.gef.CopyBuffer.ExtraCopyAction;

public class CopyGraphRewriteDialog extends Dialog {
    private final List<ExtraCopyAction> actions;

    public CopyGraphRewriteDialog(List<ExtraCopyAction> actions) {
        super(Display.getCurrent().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.actions = actions;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(700, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Localization.getString("CopyGraphRewriteDialog.title"));

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite clientArea = new Composite(scrolledComposite, SWT.BORDER);
        clientArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        clientArea.setLayout(new GridLayout());

        scrolledComposite.setContent(clientArea);

        for (ExtraCopyAction copyAction : actions) {
            final Button button = new Button(clientArea, SWT.CHECK);
            button.setText(copyAction.getDisplayName());
            button.setSelection(copyAction.isEnabled());
            button.setData(copyAction);
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ((ExtraCopyAction) button.getData()).setEnabled(button.getSelection());
                }
            });
            button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
        clientArea.layout(true, true);
        scrolledComposite.setMinHeight(clientArea.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

        return composite;
    }

}
