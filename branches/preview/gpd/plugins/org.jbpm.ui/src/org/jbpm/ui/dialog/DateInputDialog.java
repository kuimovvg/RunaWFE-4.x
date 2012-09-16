package ru.runa.bpm.ui.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.runa.bpm.ui.resource.Messages;

public class DateInputDialog extends UserInputDialog {

    public DateInputDialog(String initialValue) {
        super(Messages.getString("BSH.InputDate"), initialValue);
    }

    @Override
    protected void postCreation() {
        label.setText(Messages.getString("format") + ": dd.MM.yyyy");
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            new SimpleDateFormat("dd.MM.yyyy").parse(newValue);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

}
