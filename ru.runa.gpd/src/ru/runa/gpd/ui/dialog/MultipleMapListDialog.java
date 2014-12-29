package ru.runa.gpd.ui.dialog;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MultipleMapListDialog extends Dialog {

    private final Map<String, Boolean> values;

    private final java.util.List<String> selectedItemsList = Lists.newLinkedList();

    private final String variableTypeFilter;

    private List availableItems;

    private List selectedItems;

    private Button addButton;

    private Button removeButton;

    private Button moveUpButton;

    private Button moveDownButton;

    public MultipleMapListDialog(String variableTypeFilter, Map<String, Boolean> values) {
        super(Display.getCurrent().getActiveShell());
        this.values = values;
        this.variableTypeFilter = variableTypeFilter;
        for (Entry<String, Boolean> entry : values.entrySet()) {
            if (entry.getValue()) {
                selectedItemsList.add(entry.getKey());
            }
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        String title = isMap() ? getTitle("map") : getTitle("list");
        newShell.setText(title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        GridLayout gl = new GridLayout(4, false);
        gl.horizontalSpacing = 2;
        gl.verticalSpacing = 2;
        gl.marginBottom = 5;
        gl.marginTop = 5;

        area.setLayout(gl);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

        gridData.horizontalSpan = 4;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = false;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.minimumHeight = 100;

        availableItems = createList(area, gridData, true);

        addButton = createAddRemoveButton(area, new GridData(), getButtonText("add"), true);
        removeButton = createAddRemoveButton(area, new GridData(), getButtonText("remove"), false);
        moveUpButton = createButton(area, new GridData(), getButtonText("up"), new MoveVariableSelectionListener(true));
        moveDownButton = createButton(area, new GridData(), getButtonText("down"), new MoveVariableSelectionListener(false));

        selectedItems = createList(area, gridData, false);

        for (Map.Entry<String, Boolean> entry : values.entrySet()) {
            if (entry.getValue()) {
                selectedItems.add(entry.getKey());
            } else {
                availableItems.add(entry.getKey());
            }
        }

        return area;
    }

    private class MoveVariableSelectionListener extends LoggingSelectionAdapter {
        private final boolean up;

        public MoveVariableSelectionListener(boolean up) {
            this.up = up;
        }

        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            moveItem(selectedItems);
        }

        private void moveItem(List list) {
            if (list.getSelection() != null && list.getSelection().length == 1) {
                int index = list.getSelectionIndex();
                String selectedItem = list.getItem(index);
                list.remove(index);
                int newIndex = up ? index - 1 : index + 1;
                list.add(selectedItem, newIndex);
                list.setSelection(newIndex);
                moveButtonsUpdate(list, true);

                selectedItemsList.clear();
                for (String item : selectedItems.getItems()) {
                    selectedItemsList.add(item);
                }
            }
        }
    }

    private List createList(Composite area, GridData gridData, final boolean available) {
        List list = new List(area, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        list.setLayoutData(gridData);
        list.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                List target = available ? availableItems : selectedItems;
                String[] selection = target.getSelection();
                boolean isSelected = selection != null && selection.length > 0;
                if (!available) {
                    removeButton.setEnabled(isSelected);
                    addButton.setEnabled(false);
                    if (isSelected) {
                        availableItems.setSelection(-1);
                    }
                    moveButtonsUpdate(selectedItems, isSelected);
                } else {
                    addButton.setEnabled(isSelected);
                    removeButton.setEnabled(false);
                    if (isSelected) {
                        selectedItems.setSelection(-1);
                    }
                }
            }

        });
        return list;
    }

    private void moveButtonsUpdate(List target, boolean isSelected) {
        if (isSelected) {
            if (target.getSelectionIndex() > 0) {
                moveUpButton.setEnabled(true);
            } else {
                moveUpButton.setEnabled(false);
            }
            if (target.getSelectionIndex() < (target.getItemCount() - 1)) {
                moveDownButton.setEnabled(true);
            } else {
                moveDownButton.setEnabled(false);
            }
        } else {
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
        }
    }

    private Button createAddRemoveButton(Composite area, GridData buttonData, String text, final boolean isAddButton) {
        final Button button = new Button(area, SWT.NONE);
        button.setLayoutData(buttonData);
        button.setText(text);
        button.setEnabled(false);
        button.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {

                List fromItems = isAddButton ? availableItems : selectedItems;
                List toItems = isAddButton ? selectedItems : availableItems;
                String[] selection = fromItems.getSelection();

                if (selection != null && selection.length > 0) {
                    for (String selectedValue : selection) {
                        toItems.add(selectedValue);
                        fromItems.remove(selectedValue);
                        values.remove(selectedValue);
                        values.put(selectedValue, isAddButton);
                        if (isAddButton) {
                            selectedItemsList.add(selectedValue);
                        } else {
                            selectedItemsList.remove(selectedValue);
                        }
                    }
                    button.setEnabled(false);
                }
            }
        });
        return button;
    }

    private Button createButton(Composite area, GridData buttonData, String text, LoggingSelectionAdapter selectionAdapter) {
        final Button button = new Button(area, SWT.NONE);
        button.setLayoutData(buttonData);
        button.setText(text);
        button.setEnabled(false);
        button.addSelectionListener(selectionAdapter);
        return button;
    }

    public Object openDialog() {
        if (open() == IDialogConstants.OK_ID) {
            java.util.List<String> selected = Lists.newLinkedList();
            for (String item : selectedItemsList) {
                selected.add(item);
            }
            return Joiner.on(",").join(selected);
        }
        return null;
    }

    private boolean isMap() {
        Preconditions.checkNotNull(variableTypeFilter);
        return variableTypeFilter.equalsIgnoreCase(Map.class.getName());
    }

    private String getTitle(String key) {
        return Localization.getString(addPostfix(getPrefix("title"), key));
    }

    private String getButtonText(String key) {
        return Localization.getString(addPostfix(getPrefix("button"), key));
    }

    private String addPostfix(String src, String postfix) {
        return new StringBuilder().append(src).append(".").append(postfix).toString();
    }

    private String getPrefix(String prefix) {
        return this.getClass().getSimpleName() + "." + prefix;
    }
}
