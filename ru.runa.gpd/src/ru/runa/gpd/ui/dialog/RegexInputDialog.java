package ru.runa.gpd.ui.dialog;

import java.util.regex.Pattern;

import ru.runa.gpd.Localization;

public class RegexInputDialog extends UserInputDialog {

    public RegexInputDialog(String initialValue) {
        super(Localization.getString("BSH.InputRegexp"), initialValue);
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            Pattern.compile(newValue);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
