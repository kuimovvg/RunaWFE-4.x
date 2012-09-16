package ru.runa.bpm.ui.dialog;

import ru.runa.bpm.ui.resource.Messages;

public class NumberInputDialog extends UserInputDialog {

    public NumberInputDialog(String initialValue) {
        super(Messages.getString("BSH.InputNumber"), initialValue);
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            Double.parseDouble(newValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
