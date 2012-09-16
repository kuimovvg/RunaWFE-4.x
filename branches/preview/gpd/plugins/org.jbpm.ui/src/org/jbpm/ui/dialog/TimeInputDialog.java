package ru.runa.bpm.ui.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.runa.bpm.ui.resource.Messages;

public class TimeInputDialog extends UserInputDialog {

    public TimeInputDialog(String initialValue) {
        super(Messages.getString("BSH.InputTime"), initialValue);
    }

    @Override
    protected void postCreation() {
        label.setText(Messages.getString("format") + ": HH:MM");
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            new SimpleDateFormat("H:mm").parse(newValue);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

}
