package ru.runa.bpm.ui.dialog;

import java.util.regex.Pattern;

import ru.runa.bpm.ui.resource.Messages;

public class RegexInputDialog extends UserInputDialog {

    public RegexInputDialog(String initialValue) {
        super(Messages.getString("BSH.InputRegexp"), initialValue);
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
