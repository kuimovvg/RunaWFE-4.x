package ru.runa.gpd.ui.custom;

import java.util.Date;
import java.util.Map;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.dialog.DateInputDialog;
import ru.runa.gpd.ui.dialog.DoubleInputDialog;
import ru.runa.gpd.ui.dialog.LongInputDialog;
import ru.runa.gpd.ui.dialog.UserInputDialog;

import com.google.common.collect.Maps;

public class TypedUserInputCombo extends Combo {
    public static final String INPUT_VALUE = Localization.getString("InputValue");

    private static Map<String, Class<? extends UserInputDialog>> dialogClassesForTypes = Maps.newHashMap();
    static {
        dialogClassesForTypes.put(String.class.getName(), UserInputDialog.class);
        dialogClassesForTypes.put(Date.class.getName(), DateInputDialog.class);
        dialogClassesForTypes.put(Integer.class.getName(), LongInputDialog.class);
        dialogClassesForTypes.put(Long.class.getName(), LongInputDialog.class);
        dialogClassesForTypes.put(Number.class.getName(), DoubleInputDialog.class);
        dialogClassesForTypes.put(Double.class.getName(), DoubleInputDialog.class);
    }

    private String userInputValue;
    private String typeClassName;
    private boolean showEmptyValue = true;
    private boolean readOnlyOnMissedTypeEditor = true;
    private String previousComboTextValue = "";

    public TypedUserInputCombo(Composite parent, String oldUserInputValue) {
        super(parent, SWT.READ_ONLY);
        this.userInputValue = oldUserInputValue;
        addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent event) throws Exception {
                if (!INPUT_VALUE.equals(getText())) {
                    previousComboTextValue = getText();
                    return;
                }
                Class<? extends UserInputDialog> userInputDialogClass = dialogClassesForTypes.get(typeClassName);
                if (userInputDialogClass == null) {
                    if (readOnlyOnMissedTypeEditor) {
                        return;
                    }
                    userInputDialogClass = dialogClassesForTypes.get(String.class.getName());
                }
                UserInputDialog userInputDialog = userInputDialogClass.newInstance();
                userInputDialog.setInitialValue(userInputValue);
                if (Window.OK == userInputDialog.open()) {
                    if (userInputValue != null) {
                        remove(0);
                    }
                    userInputValue = userInputDialog.getUserInput();
                    add(userInputValue, 0);
                    select(0);
                } else {
                    setText(previousComboTextValue);
                }
            }
        });
    }
    
    @Override
    public void setText(String string) {
        previousComboTextValue = string;
        super.setText(string);
    }

    @Override
    protected void checkSubclass() {
    }

    public void setReadOnlyOnMissedTypeEditor(boolean readOnlyOnMissedTypeEditor) {
        this.readOnlyOnMissedTypeEditor = readOnlyOnMissedTypeEditor;
    }

    public void setShowEmptyValue(boolean showEmptyValue) {
        this.showEmptyValue = showEmptyValue;
    }

    public void setTypeClassName(String typeClassName) {
        if (this.typeClassName != null) {
            // refresh mode
            removeAll();
        }
        this.typeClassName = typeClassName;
        if (showEmptyValue) {
            add("", 0);
        }
        if (Boolean.class.getName().equals(typeClassName)) {
            add("true");
            add("false");
            return;
        }
        if (userInputValue != null) {
            add(userInputValue, 0);
            select(0);
        }
        if (readOnlyOnMissedTypeEditor && dialogClassesForTypes.get(typeClassName) == null) {
            return;
        }
        add(INPUT_VALUE);
    }
}
