package ru.runa.gpd.ui.dialog;

import ru.runa.gpd.Localization;

public class NumberInputDialog extends UserInputDialog {

    public NumberInputDialog(String initialValue) {
        super(Localization.getString("BSH.InputNumber"), initialValue);
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
