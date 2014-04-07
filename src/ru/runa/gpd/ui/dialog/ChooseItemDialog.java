package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;

public class ChooseItemDialog extends Dialog {
    private List<? extends Object> items;
    private Object selectedItem;
    private final String dialogText;
    private final String labelText;
    private final boolean useFilter;
    private LabelProvider labelProvider;
    private Text filterText;
    private ListViewer itemsList;

    public ChooseItemDialog(String dialogText, String labelText, boolean useFilter) {
        super(Display.getCurrent().getActiveShell());
        this.dialogText = dialogText;
        this.labelText = labelText;
        this.useFilter = useFilter;
    }

    public void setLabelProvider(LabelProvider labelProvider) {
        this.labelProvider = labelProvider;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, true);
        area.setLayout(layout);
        if (labelText != null) {
            Label label = new Label(area, SWT.NO_BACKGROUND);
            label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
            label.setText(labelText);
        }
        if (useFilter) {
            filterText = new Text(area, SWT.BORDER);
            filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            filterText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    itemsList.refresh();
                }
            });
        }
        itemsList = new ListViewer(area, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        GridData listData = new GridData(GridData.FILL_BOTH);
        listData.minimumHeight = 200;
        listData.heightHint = 200;
        listData.minimumWidth = 100;
        itemsList.getControl().setLayoutData(listData);
        itemsList.setContentProvider(new ArrayContentProvider());
        itemsList.setInput(items);
        if (useFilter) {
            itemsList.addFilter(new ItemsFilter());
        }
        if (labelProvider != null) {
            itemsList.setLabelProvider(labelProvider);
        }
        if (selectedItem != null) {
            itemsList.setSelection(new StructuredSelection(selectedItem));
        }
        itemsList.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                selectedItem = ((IStructuredSelection) event.getSelection()).getFirstElement();
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            }
        });
        itemsList.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            protected void onDoubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        return area;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        return control;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(dialogText);
    }

    public Object getSelectedItem() {
        return selectedItem;
    }
    
    public void setSelectedItem(Object selectedItem) {
        this.selectedItem = selectedItem;
        if (itemsList != null) {
            if (selectedItem != null) {
                itemsList.setSelection(new StructuredSelection(selectedItem));
            } else {
                itemsList.setSelection(new StructuredSelection());
            }
        }
    }

    public List<? extends Object> getItems() {
        return items;
    }

    public void setItems(List<? extends Object> types) {
        this.items = types;
    }

    public class ItemsFilter extends ViewerFilter {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            String elementText = labelProvider != null ? labelProvider.getText(element) : element.toString();
            return elementText.toLowerCase().startsWith(filterText.getText().toLowerCase());
        }
    }
}
